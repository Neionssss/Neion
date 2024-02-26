package neion.funnymap

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.events.PacketReceiveEvent
import neion.funnymap.map.MapUtils
import neion.funnymap.map.Puzzle
import neion.funnymap.map.Room
import neion.funnymap.map.RoomState
import neion.ui.GuiRenderer
import neion.utils.APIHandler
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.TextUtils
import neion.utils.TextUtils.matchesAny
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.event.ClickEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// Modified some parts from fm, but most are the same
/**
 * Many parts of this code are modified from [Skytils](https://github.com/Skytils/SkytilsMod/blob/1.x/src/main/kotlin/gg/skytils/skytilsmod/features/impl/dungeons/ScoreCalculation.kt).
 */
object RunInformation {

    var deathCount = 0
    val completedPuzzles: Int
        get() = puzzles.count { it.value }
    var totalPuzzles = 0
    var cryptsCount = 0
    var secretsFound = 0
    var secretPercentage = 0f
    val secretTotal: Int
        get() = (secretsFound / (secretPercentage + 0.0001f) + 0.5).toInt()
    var minSecrets = 0
    var mimicKilled = false
    private var completedRooms = 0
    val completedRoomsPercentage
        get() = (completedRooms + (if (!Location.inBoss) 1 else 0) + (if (!bloodDone) 1 else 0)) / (if (totalRooms == 0) 36 else totalRooms).toFloat()
    var bloodDone = false
    private val totalRooms: Int
        get() = (completedRooms / (clearedPercentage + 0.0001f) + 0.4).toInt()
    private var clearedPercentage = 0f
    var startTime = 0L
    var timeElapsed = 0
    var mimicOpenTime = 0L
    var mimicPos: BlockPos? = null
    val puzzles = mutableMapOf<Puzzle, Boolean>()
    var failedPuzzles = 0

    var trapType = ""
    private val paul = APIHandler.hasBonusPaulScore()
        get() = field || FMConfig.paulBonus
    var score = 0
    var message300 = false
        get() = field.also { field = true }
    var message270 = false
        get() = field.also { field = true }
    var keys = 0
    var bloodKey = false
    var started = false
    var mimicFound = false

    private val deathsRegex = Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r")
    private val puzzleCountRegex = Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r")
    private val failedPuzzleRegex = Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val solvedPuzzleRegex = Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val roomCompletedPattern = Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r")
    private val dungeonClearedPattern = Regex("Cleared: (?<percentage>\\d+)% \\(\\d+\\)")
    private val timeElapsedPattern = Regex("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
    private val keyGainRegex = listOf(
        Regex(".+ §r§ehas obtained §r§a§r§.+Wither Key§r§e!§r"),
        Regex("§r§eA §r§a§r§.+Wither Key§r§e was picked up!§r")
    )
    private val keyBloodRegex = listOf(
        Regex(".+ §r§ehas obtained §r§a§r§.+Blood Key§r§e!§r"),
        Regex("§r§eA §r§a§r§.+Blood Key§r§e was picked up!§r")
    )
    private val keyUseRegex = listOf(
        Regex("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"),
        Regex("§r§a.+§r§a opened a §r§8§lWITHER §r§adoor!§r"),
    )

    fun reset() {
        keys = 0
        deathCount = 0
        totalPuzzles = 0
        cryptsCount = 0
        secretsFound = 0
        secretPercentage = 0f
        completedRooms = 0
        clearedPercentage = 0f
        timeElapsed = 0
        startTime = 0L
        mimicOpenTime = 0L
        failedPuzzles = 0

        trapType = ""

        started = false
        bloodKey = false
        bloodDone = false
        message270 = false
        message300 = false
        mimicKilled = false
        mimicFound = false
        mimicPos = null

        puzzles.clear()
    }

    @SubscribeEvent
    fun onScoreboard(e: PacketReceiveEvent) {
        if (!inDungeons) return
        when (val packet = e.packet) {
            is S3EPacketTeams -> if (packet.action == 2) {

                minSecrets =
                    ceil(ceil(secretTotal * getSecretPercent()) * (40 - getBonusScore() + getDeathDeduction()) / 40).toInt()

                val line = packet.players.joinToString(" ", prefix = packet.prefix, postfix = packet.suffix)
                    .stripControlCodes()

                if (line.startsWith("Cleared: ")) {
                    val match = dungeonClearedPattern.matchEntire(line)?.groups ?: return
                    clearedPercentage = (match["percentage"]?.value?.toFloatOrNull()?.div(100f)) ?: clearedPercentage
                } else if (line.startsWith("Time Elapsed:")) {
                    val match = timeElapsedPattern.matchEntire(line)?.groups ?: return
                    val hours = match["hrs"]?.value?.toIntOrNull() ?: 0
                    val minutes = match["min"]?.value?.toIntOrNull() ?: 0
                    val seconds = match["sec"]?.value?.toIntOrNull() ?: 0
                    timeElapsed = hours * 3600 + minutes * 60 + seconds
                }
            }

            is S38PacketPlayerListItem -> if (packet.action.equalsOneOf(
                    S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME,
                    S38PacketPlayerListItem.Action.ADD_PLAYER
                )
            ) {
                packet.entries.forEach {
                    updateFromTabList(it?.displayName?.formattedText ?: it?.profile?.name ?: return@forEach)
                }
            }

            is S02PacketChat -> if (packet.type != 2.toByte()) {
                val text = packet.chatComponent.unformattedText.stripControlCodes()
                if (text.startsWith("Party > ") || (text.contains(":") && !text.contains(">"))) {
                    listOf("\$SKYTILS-DUNGEON-SCORE-MIMIC\$", "mimic dead", "mimic killed").forEach {
                        if (text.contains(it, true) && !mimicKilled) mimicKilled = true
                    }

                    listOf("blaze done", "blaze puzzle finished").forEach {
                        if (text.contains(it, true)) {
                            val puzzle =
                                puzzles.keys.find { puzzle -> puzzle.tabName == "Higher Or Lower" }
                                    ?: return
                            puzzles[puzzle] = true
                            val room = Dungeon.dungeonList.firstOrNull { tile ->
                                tile is Room && tile.data.name.equalsOneOf(
                                    "Lower Blaze",
                                    "Higher Blaze"
                                )
                            } ?: return
                            PlayerTracker.roomStateChange(room, room.state, RoomState.CLEARED)
                            room.state = RoomState.CLEARED
                        }
                    }
                }
                if (FMConfig.teamInfo) {
                    if (packet.chatComponent.siblings.any {
                            it.chatStyle?.chatClickEvent?.run { action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats" } == true
                        }) PlayerTracker.onDungeonEnd()
                }
                val form = packet.chatComponent.formattedText
                if (form.matchesAny(keyGainRegex)) keys++
                if (form.contains(keyUseRegex[1])) keys--
                if (form.matchesAny(keyBloodRegex)) bloodKey = true
                if (form.contains(keyUseRegex[0])) bloodKey = false
                if (text.startsWith("[BOSS] Maxor: ") || Location.entryMessages.any { it == text }) Location.inBoss = true

                when (text) {
                    "Starting in 4 seconds." -> for (i in listOf(5, 9, 13, 17, 1)) (MapUtils.getDungeonTabList()
                        ?: return)[i].first.locationSkin

                    "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                        MapUpdate.getPlayers()
                        startTime = System.currentTimeMillis()
                        started = true
                    }

                    "[BOSS] The Watcher: You have proven yourself. You may pass." -> bloodDone = true
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntityDeath(e: LivingDeathEvent) {
        if (!inDungeons || mimicKilled) return
        val entity = e.entity as? EntityZombie ?: return
        for (i in 0..3) if (entity.isChild && entity.getCurrentArmor(i) == null) setMimicKilled()
    }

    private fun updateFromTabList(text: String) {
        when {
            text.contains("Team Deaths:") -> deathCount = deathsRegex.firstResult(text)?.toIntOrNull() ?: deathCount
            text.contains("✔") -> {
                val puzzleName = solvedPuzzleRegex.firstResult(text) ?: return
                if (puzzleName == "???") return
                val puzzle = puzzles.keys.find { it.tabName == puzzleName }
                if (puzzle == null) {
                    if (puzzles.size < totalPuzzles) Puzzle.fromName(puzzleName)?.let { puzzles.putIfAbsent(it, true) }
                } else puzzles[puzzle] = true
            }

            text.contains("✖") -> {
                val puzzleName = failedPuzzleRegex.firstResult(text) ?: return
                if (puzzleName == "???") return
                val puzzle = puzzles.keys.find { it.tabName == puzzleName }
                failedPuzzles++
                if (puzzle == null) {
                    if (puzzles.size < totalPuzzles) Puzzle.fromName(puzzleName)?.let { puzzles.putIfAbsent(it, false) }
                } else puzzles[puzzle] = false
            }

            text.contains("Crypts:") -> cryptsCount = cryptsPattern.firstResult(text)?.toIntOrNull() ?: cryptsCount
            text.contains("Secrets Found:") -> {
                if (text.contains("%")) secretPercentage = secretsFoundPercentagePattern.firstResult(text)?.toFloatOrNull()?.div(100f) ?: secretPercentage
                else secretsFound = secretsFoundPattern.firstResult(text)?.toIntOrNull() ?: secretsFound
            }
            text.contains("Completed Rooms") -> completedRooms = roomCompletedPattern.firstResult(text)?.toIntOrNull() ?: completedRooms
        }
    }

    fun checkMimicDeath() {
        if (!mimicKilled) {
            mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }.forEach {
                if (it.pos != it) {
                    mimicOpenTime = System.currentTimeMillis()
                    mimicPos = it.pos
                }
            }
            if (mimicOpenTime != 0L && System.currentTimeMillis() - mimicOpenTime > 750 && mc.thePlayer.getDistanceSq(
                    mimicPos) < 400 && mc.theWorld.loadedEntityList.none {
                    it is EntityZombie && it.isChild && it.getCurrentArmor(3)
                        ?.getSubCompound("SkullOwner", false)
                        ?.getString("Id") == "bcb486a4-0cb5-35db-93f0-039fbdde03f0"
                }) setMimicKilled()
        }
    }

    fun updatePuzzleCount(tabList: List<Pair<NetworkPlayerInfo, String>>) {
        if (totalPuzzles != 0) return
        val puzzleCount = tabList.find { it.second.contains("Puzzles:") }?.second ?: return
        totalPuzzles = puzzleCountRegex.firstResult(puzzleCount)?.toIntOrNull() ?: totalPuzzles
    }

    private fun setMimicKilled() {
        if (mimicKilled) return
        mimicKilled = true
        if (FMConfig.mimicMessageEnabled) TextUtils.sendPartyChatMessage(FMConfig.mimicMessage)
    }


    // https://i.imgur.com/ifMCejI.png
    fun updateScore() {
        val roomPercent = completedRoomsPercentage.coerceAtMost(1f)
        val explore = (60 * roomPercent + 40 * (secretPercentage / getSecretPercent()).coerceAtMost(1f)).toInt()
        val skill = 20 + ((80 * roomPercent).toInt() - (totalPuzzles - completedPuzzles) * 10 - getDeathDeduction()).coerceAtLeast(0)
        score = skill + explore + 100 + getBonusScore()
        if (score >= 300 && !message300) {
            message270 = true
            if (FMConfig.scoreMessage != 0) TextUtils.sendPartyChatMessage(FMConfig.message300)
            if (FMConfig.scoreTitle != 0) {
                mc.thePlayer.playSound("random.orb", 1f, 0.5.toFloat())
                GuiRenderer.displayTitle(FMConfig.message300, 40)
            }
            if (FMConfig.timeTo300) TextUtils.info("§3300 Score§7: §a${timeElapsed.toDuration(DurationUnit.SECONDS)}")
        } else if (score >= 270 && !message270) {
            if (FMConfig.scoreMessage == 2) TextUtils.sendPartyChatMessage(FMConfig.message270)
            if (FMConfig.scoreTitle == 2) {
                mc.thePlayer.playSound("random.orb", 1f, 0.5.toFloat())
                GuiRenderer.displayTitle(FMConfig.message270, 40)
            }
        }
    }

    private fun getDeathDeduction(): Int {
        var deathDeduction = deathCount * 2
        if (FMConfig.scoreAssumeSpirit) deathDeduction -= 1
        return deathDeduction.coerceAtLeast(0)
    }

    private fun getBonusScore(): Int {
        var score = 0
        score += cryptsCount.coerceAtMost(5)
        if (mimicKilled) score += 2
        if (paul) score += 10
        return score
    }

    private fun getSecretPercent(): Float {
        if (Location.masterMode) return 1f
        return when (Location.dungeonFloor) {
            0 -> .3f
            1 -> .3f
            2 -> .4f
            3 -> .5f
            4 -> .6f
            5 -> .7f
            6 -> .85f
            else -> 1f
        }
    }

    private fun Regex.firstResult(input: CharSequence): String? {
        return this.matchEntire(input)?.groups?.get(1)?.value
    }
}