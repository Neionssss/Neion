package neion.ui

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.funnymap.RunInformation
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import neion.utils.Utils.equalsOneOf

class ScoreElement : MovableGuiElement("Score") {
    val lines: List<String>
        get() {
            val list: MutableList<String> = mutableListOf()
            if (MapConfig.scoreTotalScore) list.add(getScore())
            if (MapConfig.scoreSecrets != 0) list.add(getSecrets())

            list.add("split")

            if (MapConfig.scoreCrypts) list.add(getCrypts())
            if (MapConfig.scoreMimic && Location.dungeonFloor.equalsOneOf(6,7)) list.add(getMimic())
            if (MapConfig.scoreDeaths) list.add(getDeaths())

            list.add("split1")

            if (MapConfig.scorePuzzles != 0) list.add(getPuzzles())

            return list
        }
    override val h: Int = mc.fontRendererObj.FONT_HEIGHT * lines.size
    override val w: Int = mc.fontRendererObj.getStringWidth("Crypts: 0   Mimic: ✘   Deaths: 10   Puzzles: 5")

    override fun render() {
        RenderUtil.renderText(lines.takeWhile { it != "split" }.joinToString(separator = "    "), 0, 0)
        RenderUtil.renderText(lines.takeWhile { it != "split1" }.takeLastWhile { it != "split" }.joinToString(separator = "    "), 0, 9)
        RenderUtil.renderText(lines.takeLastWhile { it != "split" && it != "split1" }.joinToString(separator = "    "), 0, 18)
    }

    override fun shouldRender() = MapConfig.mapShowRunInformation == 2 && inDungeons || MapConfig.scoreHideInBoss && !Location.inBoss

    companion object {

        private fun getScore(): String {
            var line = if (MapConfig.scoreMinimizedName) "" else "§7Score: "
            line += "${when {
                RunInformation.score < 270 -> "§c"
                RunInformation.score < 300 -> "§e"
                else -> "§a"
            }}${RunInformation.score}"

            return line
        }

        private fun getSecrets(): String {
            var line = if (MapConfig.scoreMinimizedName) "" else "§7Secrets: "
            line += "§b${RunInformation.secretsFound}§7/"
            if (MapConfig.scoreSecrets == 2) line += "§e${(RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)}§7/"
            line += "§c${RunInformation.secretTotal}"

            return line
        }

        private fun getCrypts(): String {
            var line = if (MapConfig.scoreMinimizedName) "§7C: " else "§7Crypts: "
            line += if (RunInformation.cryptsCount >= 5) "§a${RunInformation.cryptsCount}" else "§c${RunInformation.cryptsCount}"
            return line
        }

        private fun getMimic(): String {
            var line = if (MapConfig.scoreMinimizedName) "§7M: " else "§7Mimic: "
            line += if (RunInformation.mimicKilled) "§a✔" else "§c✘"
            return line
        }

        private fun getDeaths(): String {
            var line = if (MapConfig.scoreMinimizedName) "§7D: " else "§7Deaths: "
            line += "§c${RunInformation.deathCount}"
            return line
        }

        private fun getPuzzles(): String {
            val color = if (RunInformation.completedPuzzles == RunInformation.totalPuzzles) "§a" else "§c"
            var line = if (MapConfig.scoreMinimizedName) "§7P: " else "§7Puzzles: "
            line += "$color${RunInformation.completedPuzzles}"
            if (MapConfig.scorePuzzles == 2) line += "§7/$color${RunInformation.totalPuzzles}"
            line += if (RunInformation.failedPuzzles == 0) "§a✔" else if (RunInformation.failedPuzzles > 0) "§c✘" else "§4I'm not sure you playing the right game"
            return line
        }
    }
}
