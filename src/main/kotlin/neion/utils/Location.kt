package neion.utils

import neion.Neion.Companion.mc
import neion.features.ForceSkyblock
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.*

object Location {

    var inSkyblock = false
    var inDungeons = false
    var dungeonFloor = -1
    var masterMode = false
    var inBoss = false

    fun isHypixel() = mc.theWorld != null &&
            !mc.isSingleplayer &&
            (mc.thePlayer?.clientBrand?.lowercase()?.contains("hypixel") == true ||
                    mc.currentServerData?.serverIP?.lowercase(Locale.getDefault())?.contains("hypixel")!!)

    fun onTick() {
        if (ForceSkyblock.enabled) {
            inSkyblock = true
            inDungeons = true
            dungeonFloor = 7
        } else {
            inSkyblock = isHypixel() && mc.theWorld.scoreboard?.getObjectiveInDisplaySlot(1)?.name == "SBScoreboard"

            if (!inDungeons) {
                getLines().find {
                    cleanLine(it).run {
                        contains("The Catacombs (") && !contains("Queue")
                    }
                }?.let {
                    inDungeons = true
                    val line = it.substringBefore(")")
                    dungeonFloor = line.lastOrNull()?.digitToIntOrNull() ?: 0
                    masterMode = line[line.length - 2] == 'M'
                }
            }
        }
    }

    // --------------
    fun cleanLine(scoreboard: String): String = scoreboard.stripControlCodes().filter { it.code in 32..126 }

    fun getLines(): List<String> {
        return mc.theWorld?.scoreboard?.run {
            getSortedScores(getObjectiveInDisplaySlot(1) ?: return emptyList())
                .filter { it?.playerName?.startsWith("#") == false }
                .let { if (it.size > 15) it.drop(15) else it }
                .map { ScorePlayerTeam.formatPlayerName(getPlayersTeam(it.playerName), it.playerName) }
        } ?: emptyList()
    }
}
