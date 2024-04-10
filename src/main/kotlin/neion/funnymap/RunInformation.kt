package neion.funnymap

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.events.PacketReceiveEvent
import neion.funnymap.map.*
import neion.utils.APIHandler
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.TextUtils
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.matchesAny
import neion.utils.TextUtils.stripControlCodes
import neion.utils.Utils.equalsOneOf
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import kotlin.time.Duration.Companion.milliseconds

// Modified some parts from fm, but most are the same
/**
 * Many parts of this code are modified from [Skytils](https://github.com/Skytils/SkytilsMod/blob/1.x/src/main/kotlin/gg/skytils/skytilsmod/features/impl/dungeons/ScoreCalculation.kt).
 */
object RunInformation {

    var deathCount = 0
    val completedPuzzles = Puzzle.entries.count { it.completed }
    var totalPuzzles = 0
    var cryptsCount = 0
    var secretsFound = 0
    private var secretPercentage = 0f
    val secretTotal = (secretsFound / (secretPercentage + 0.0001f) + 0.5).toInt()
    var minSecrets = 0
    var mimicKilled = false
    private var completedRooms = 0
    private var clearedPercentage = 0f
    private val totalRooms = (completedRooms / (clearedPercentage + 0.0001f) + 0.4).toInt()
    var startTime = 0L
    private var timeElapsed: String? = null
    var failedPuzzles = 0
    private val paul = APIHandler.hasBonusPaulScore()
        get() = field || MapConfig.paulBonus
    var score = 0
    private var message300 = false
    private var message270 = false
    var keys = 0
    var bloodKey = false

    fun onScoreboard(e: PacketReceiveEvent) {
        if (!inDungeons) return
        when (val packet = e.packet) {
            is S3EPacketTeams -> if (packet.action == 2) {
                val line = stripControlCodes(packet.players.joinToString(" ", packet.prefix, packet.suffix))

                when {
                    line.startsWith("Cleared: ") -> clearedPercentage = Regex("Cleared: (?<percentage>\\d+)% \\(\\d+\\)").matchEntire(line)?.groups?.get("percentage")?.value?.toFloatOrNull()?.div(100f) ?: clearedPercentage
                    line.startsWith("Time Elapsed:") -> getTime(line)
                }
            }

            is S38PacketPlayerListItem -> if (packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) packet.entries.forEach { itw ->

                with(itw?.displayName?.formattedText ?: itw?.profile?.name ?: return) {
                    when {
                        contains("Team Deaths:") -> deathCount = getIntResult(Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r"), this) ?: deathCount
                        contains("Crypts:") -> cryptsCount = getIntResult(Regex("§r Crypts: §r§6(?<crypts>\\d+)§r"), this) ?: cryptsCount
                        contains("Secrets Found:") -> if (contains("%")) secretPercentage = firstResult(Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r"), this)?.toFloatOrNull()?.div(100f) ?: secretPercentage else secretsFound = firstResult(Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r"), this)?.toIntOrNull() ?: secretsFound
                        contains("Completed Rooms") -> completedRooms = getIntResult(Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r"), this) ?: completedRooms
                    }
                    handlePuzzles(this)
                }
            }

            is S02PacketChat -> if (packet.type != 2.toByte()) handleChatPacket(packet.chatComponent)
        }
    }

    fun onEntityDeath(e: LivingDeathEvent) {
        if (!inDungeons || mimicKilled) return
        val entity = e.entity as? EntityZombie ?: return
        for (i in 0..3) if (!mimicKilled && entity.isChild && entity.getCurrentArmor(i) == null) mimicKilled = true
    }

    // https://i.imgur.com/ifMCejI.png
    fun updateScore() {
        val secretCompletionPercent = when (Location.masterMode) {
            true -> 1f
            false -> when (Location.dungeonFloor) {
                0, 1 -> 0.3f
                2 -> 0.4f
                3 -> 0.5f
                4 -> 0.6f
                5 -> 0.7f
                6 -> 0.85f
                else -> 1f
            }
        }

        val roomCompletionPercent = (completedRooms + if (!Location.inBoss) 1 else 0).toFloat() / (totalRooms * 1.0f).coerceAtLeast(1f)

        val bonusScore = cryptsCount.coerceAtMost(5) + (if (mimicKilled) 2 else 0) + (if (paul) 10 else 0)

        val roomScore = 60 * roomCompletionPercent + 40 * maxOf(0f, (secretTotal * secretCompletionPercent * ((40 - bonusScore + getDeathDeduction()) / 40f)).coerceAtMost(1f))

        val baseScore = 20 + 80 * roomCompletionPercent + 10 * completedPuzzles

        val finalScore = (baseScore + roomScore + 100 + bonusScore - getDeathDeduction()).toInt()
        score = finalScore.also { if (MapConfig.timeTo300 && it >= 300 && !message300) { message300 = true; mc.thePlayer.playSound("random.orb", 1f, 0.5f); TextUtils.info("§3300 Score§7: §a$timeElapsed") } }
    }

    private fun getDeathDeduction(): Int {
        var deathDeduction = deathCount * 2
        if (MapConfig.scoreAssumeSpirit) deathDeduction -= 1
        return deathDeduction.coerceAtLeast(0)
    }

    fun onDungeonEnd() {
        Dungeon.players.values.forEach { playerData ->

            playerData.roomVisits.add(Pair(System.currentTimeMillis() - startTime - playerData.lastTime, playerData.lastRoom!!))

            val formattedName = playerData.formattedName
            val allClearedRooms = MapUpdate.roomClears.filter { it.value.contains(formattedName) }
            val soloClearedRooms = allClearedRooms.filter { it.value.size == 1 }

            val clearedRoomsCount = allClearedRooms.size
            val soloCleared = if (soloClearedRooms.size == clearedRoomsCount) clearedRoomsCount
            else "${soloClearedRooms.size}-$clearedRoomsCount"


            mc.thePlayer.addChatMessage(ChatComponentText("§f§0[Neion]§f§r §3$formattedName §f> ").appendSibling(ChatComponentText("§b${APIHandler.getSecrets(playerData.uuid) - playerData.startingSecrets} §3secrets")).appendText(" §6| ").appendSibling(ChatComponentText("§b$soloCleared §3Rooms").apply { chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(playerData.roomVisits.groupBy { it.first }.entries.joinToString(separator = "\n", prefix = "$formattedName's §eRoom Times:\n") { (room, times) -> "§6$room §a- §b${times.sumOf { it.first }.milliseconds}" })))}))
        }
    }


    private fun handleChatPacket(component: IChatComponent) {
        val text = stripControlCodes(component.unformattedText)
        if (MapConfig.teamInfo && component.siblings.any { it.chatStyle?.chatClickEvent?.run { action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats" }!! }) onDungeonEnd()

        processChatEvents(text,component.formattedText)

        if (text == "Starting in 4 seconds.") setupPlayers()
    }

    private fun processChatEvents(text: String, formattedText: String) {
        when {
            formattedText.matchesAny(
                Regex(".+ §r§ehas obtained §r§a§r§.+Wither Key§r§e!§r"),
                Regex("§r§eA §r§a§r§.+Wither Key§r§e was picked up!§r")
            ) -> keys++

            formattedText.matchesAny(
                Regex("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"),
                Regex("§r§a.+§r§a opened a §r§8§lWITHER §r§adoor!§r")
            ) -> keys--

            formattedText.matchesAny(
                Regex(".+ §r§ehas obtained §r§a§r§.+Blood Key§r§e!§r"),
                Regex("§r§eA §r§a§r§.+Blood Key§r§e was picked up!§r")
            ) -> bloodKey = true

            formattedText.contains("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r") -> bloodKey = false

            text.startsWith("[BOSS] Maxor: ") ||
                    text.equalsOneOf(
                        "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.",
                        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
                        "[BOSS] The Professor: I was burdened with terrible news recently...",
                        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
                        "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.",
                        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"
                    ) -> Location.inBoss = true

            text.containsAny("mimic dead", "mimic killed") && !mimicKilled -> mimicKilled = true

            text.containsAny("blaze done", "blaze puzzle finished") -> {
                fromName("Higher Or Lower")?.completed = true
                Dungeon.dungeonList.firstOrNull { (it as? Room)?.data?.name.equalsOneOf("Lower Blaze", "Higher Blaze") }?.state = RoomState.CLEARED
            }
        }
    }

    private fun setupPlayers() {
        for (i in listOf(5, 9, 13, 17, 1)) MapUtils.getDungeonTabList()?.get(i)?.first?.let {
            val name = stripControlCodes(
                it.displayName.unformattedText
                    .trim()
                    .substringAfterLast("] ")
                    .split(" ")[0]
            )
            if (name != "") Dungeon.players[name] = DungeonPlayer(it.locationSkin).apply {
                setData(mc.theWorld.getPlayerEntityByName(name))
                colorPrefix = it.displayName.formattedText.substringBefore(name, "f").last()
                this.name = name
                icon = "icon-$name"
            }
        }
        startTime = System.currentTimeMillis()
    }


    private fun handlePuzzles(name: String) {
        val regex = firstResult(Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l(?<completion>.)§r§7] §.+"), name) ?: return
        if (regex == "???") return
        if (name.contains("✖")) {
            fromName(regex)?.completed = false
            failedPuzzles++
        } else if (name.contains("✔")) fromName(regex)?.completed = true
    }

    private fun getTime(line: String) {
        val match = Regex("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?<sec>\\d+)s?").matchEntire(line)?.groups
        timeElapsed = "${match?.get("min")?.value?.toIntOrNull()} min. ${match?.get("sec")?.value?.toIntOrNull()} seconds"
    }

    private fun getIntResult(regex: Regex, input: String) = regex.matchEntire(input)?.groups?.get(1)?.value?.toIntOrNull()

    fun firstResult(regex: Regex, input: String) = regex.matchEntire(input)?.groups?.get(1)?.value

    private fun fromName(name: String) = Puzzle.entries.find { name.equalsOneOf(it.roomDataName, it.tabName) }



    fun reset() {
        keys = 0
        deathCount = 0
        totalPuzzles = 0
        cryptsCount = 0
        secretsFound = 0
        secretPercentage = 0f
        completedRooms = 0
        clearedPercentage = 0f
        timeElapsed = null
        startTime = 0L
        failedPuzzles = 0
        bloodKey = false
        message270 = false
        message300 = false
        mimicKilled = false
        Puzzle.entries.forEach { it.completed = false }
        MapUpdate.roomClears.clear()
        MapUpdate.rooms.clear()
    }
}