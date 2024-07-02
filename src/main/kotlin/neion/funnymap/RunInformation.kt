package neion.funnymap

import neion.events.ChatEvent
import neion.events.PacketReceiveEvent
import neion.funnymap.map.Puzzle
import neion.funnymap.map.Room
import neion.funnymap.map.RoomState
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.MapUtils
import neion.utils.TextUtils
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.matchesAny
import neion.utils.TextUtils.stripControlCodes
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil

// Modified some parts from fm, but most are the same
/**
 * Many parts of this code are modified from [Skytils](https://github.com/Skytils/SkytilsMod/blob/1.x/src/main/kotlin/gg/skytils/skytilsmod/features/impl/dungeons/ScoreCalculation.kt).
 */
object RunInformation {

    var deathCount = 0
    val completedPuzzles: Int
        get() = Puzzle.entries.count { it.completed == true }
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
        get() = (completedRooms + (if (!bloodDone) 1 else 0) + (if (!Location.inBoss) 1 else 0)) / (if (totalRooms == 0) 36 else totalRooms).toFloat()
    private val totalRooms: Int
        get() = (completedRooms / (clearedPercentage + 0.0001f) + 0.4).toInt()
    private var clearedPercentage = 0f

    var keys = 0
    var bloodKey = false
    var bloodDone = false
    var started = false
    var mimicFound = false
    private var firstDeath = false
    var firstDeathSpirit = false

    private val keyGainRegex = listOf(
        Regex(".+ §r§ehas obtained §r§a§r§.+Wither Key§r§e!§r"),
        Regex("§r§eA §r§a§r§.+Wither Key§r§e was picked up!§r")
    )
    private val keyBloodRegex = listOf(
        Regex(".+ §r§ehas obtained §r§a§r§.+Blood Key§r§e!§r"),
        Regex("§r§eA §r§a§r§.+Blood Key§r§e was picked up!§r")
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

        started = false
        bloodKey = false
        bloodDone = false
        mimicKilled = false
        mimicFound = false
        firstDeath = false
        firstDeathSpirit = false
        Puzzle.entries.forEach {
            it.completed = null
        }
        MapUpdate.fairyOpened = false
    }

    @SubscribeEvent
    fun onScoreboard(e: PacketReceiveEvent) {
        if (!inDungeons) return
        when (val packet = e.packet) {
            is S3EPacketTeams -> if (packet.action == 2) {

                val maxSecrets = ceil(secretTotal * ScoreCalculation.getSecretPercent())

                minSecrets = ceil(maxSecrets * (40 - ScoreCalculation.getBonusScore() + ScoreCalculation.getDeathDeduction()) / 40).toInt()

                val line = packet.players.joinToString(" ", prefix = packet.prefix, postfix = packet.suffix).stripControlCodes()

                if (line.startsWith("Cleared: ")) {
                    val match = Regex("Cleared: (?<percentage>\\d+)% \\(\\d+\\)").matchEntire(line)?.groups ?: return
                    clearedPercentage = match["percentage"]?.value?.toFloatOrNull()?.div(100) ?: clearedPercentage
                }
            }

            is S38PacketPlayerListItem -> if (packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) packet.entries.forEach {
                (it?.displayName?.formattedText ?: it?.profile?.name)?.run {
                    when {
                        contains("Team Deaths:") -> deathCount =
                            Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r").firstResult(this)?.toIntOrNull()
                                ?: deathCount

                        contains("✔") -> {
                            val puzzleName = Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+").firstResult(this) ?: return
                            if (puzzleName == "???") return@run
                            val puzzle = fromName(puzzleName) ?: return@run
                            puzzle.completed = true
                        }

                        contains("✖") -> {
                            val puzzleName = Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+").firstResult(this) ?: return
                            if (puzzleName == "???") return@run
                            fromName(puzzleName)?.completed = false
                        }

                        contains("Crypts:") -> cryptsCount =
                            Regex("§r Crypts: §r§6(?<crypts>\\d+)§r").firstResult(this)?.toIntOrNull() ?: cryptsCount

                        contains("Secrets Found:") -> {
                            if (contains("%")) secretPercentage =
                                Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r").firstResult(this)?.toFloatOrNull()?.div(100) ?: secretPercentage
                            else secretsFound = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r").firstResult(this)?.toIntOrNull() ?: secretsFound
                        }

                        contains("Completed Rooms") -> completedRooms =
                            Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r").firstResult(this)?.toIntOrNull()
                                ?: completedRooms
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(e: ChatEvent) {
        if (e.packet.type == 2.toByte() || !inDungeons) return
        val text = e.text
        if (text.startsWith("Party > ") || (text.contains(":") && !text.contains(">"))) {
            if (text.containsAny(
                    "\$SKYTILS-DUNGEON-SCORE-MIMIC\$",
                    "mimic dead",
                    "mimic killed"
                ) && !mimicKilled
            ) mimicKilled = true
            if (text.matchesAny(Regex("Party > \\d+: blaze done"), Regex("Party > \\d+: blaze puzzle finished"))) {
                fromName("Higher Or Lower")?.completed = true
                val room = Dungeon.dungeonList.firstOrNull { tile ->
                    tile is Room && tile.data.name.equalsOneOf(
                        "Lower Blaze",
                        "Higher Blaze"
                    )
                } ?: return
                room.state = RoomState.CLEARED
            }

        }
        val form = e.packet.chatComponent.formattedText
        if (form.matchesAny(keyGainRegex)) keys++
        if (form.contains(Regex("§r§a.+§r§a opened a §r§8§lWITHER §r§adoor!§r"))) keys--
        if (form.matchesAny(keyBloodRegex)) bloodKey = true
        if (form.contains(Regex("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"))) bloodKey = false
        if (text.startsWith("[BOSS] Maxor: ") || text.equalsOneOf(
                "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.",
                "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
                "[BOSS] The Professor: I was burdened with terrible news recently...",
                "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
                "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.",
                "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"
            )
        ) Location.inBoss = true

        when (text) {
            "Starting in 4 seconds." -> {
                for (i in listOf(5, 9, 13, 17, 1)) MapUtils.getDungeonTabList()?.get(i)?.first?.locationSkin
            }

            "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                started = true
                MapUpdate.getPlayers()
            }
            "[BOSS] The Watcher: You have proven yourself. You may pass." -> bloodDone = true
        }
        if (!firstDeath && deathCount != 0 && started && text.matches(Regex("^ ☠ (\\w{1,16}) (.+) and became a ghost\\.$"))) {
            firstDeath = true
            firstDeathSpirit = Dungeon.dungeonTeammates.entries.find { text.contains(it.key) }?.value?.hasSpirit!!
            if (firstDeathSpirit) TextUtils.info("Had Spirit Pet ")
        }

    }

    @SubscribeEvent
    fun onEntityDeath(e: LivingDeathEvent) {
        if (!inDungeons || mimicKilled) return
        val entity = e.entity as? EntityZombie ?: return
        for (i in 0..3) if (entity.isChild && entity.getCurrentArmor(i) == null && !mimicKilled) mimicKilled = true
    }

    fun updatePuzzles(tabList: List<Pair<NetworkPlayerInfo,String>>) {
        if (totalPuzzles != 0) return
        totalPuzzles = tabList.find { g -> g.second.contains("Puzzles:") }?.second?.let { it1 -> Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r").firstResult(it1)?.toIntOrNull() } ?: totalPuzzles
    }

    fun Regex.firstResult(input: CharSequence) = matchEntire(input)?.groups?.get(1)?.value

    private fun fromName(name: String) = Puzzle.entries.find { name.equalsOneOf(it.roomDataName, it.tabName) }

}