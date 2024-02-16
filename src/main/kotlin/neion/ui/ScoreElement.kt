package neion.ui

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.MapRender
import neion.funnymap.RunInformation
import neion.funnymap.ScoreCalculation
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import net.minecraft.client.gui.FontRenderer

class ScoreElement : MovableGuiElement() {
    override var x: Int by FMConfig::scoreX
    override var y: Int by FMConfig::scoreY
    override val h: Int
        get() = fr.FONT_HEIGHT * MapRender.lines.size
    override val w: Int = fr.getStringWidth("Crypts: 0   Mimic: ✘   Deaths: 10   Puzzles: 5")
    override var scale: Float by FMConfig::scoreScale

    override fun render() {
        val lineOne = MapRender.lines.takeWhile { it != "split" }.joinToString(separator = "    ")
        val lineTwo = MapRender.lines.takeWhile { it != "split1" }.takeLastWhile { it != "split" }.joinToString(separator = "    ")
        val lineThree = MapRender.lines.takeLastWhile { it != "split" && it != "split1" }.joinToString(separator = "    ")
        val l1sw = -fr.getStringWidth(lineOne) / 2
        val l2Sw = -fr.getStringWidth(lineTwo) / 2
        RenderUtil.renderText(lineOne, 0, l1sw)
        RenderUtil.renderText(lineTwo, 0, if (l1sw == 0) 0 else 9)
        RenderUtil.renderText(lineThree, 0, if (l2Sw.and(l1sw) == 0) 0 else if (l2Sw.or(l1sw) == 0) 9 else 18)
    }

    override fun shouldRender(): Boolean {
        if (FMConfig.mapShowRunInformation != 2 || !inDungeons) return false
        if (FMConfig.scoreHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }

    companion object {
        val fr: FontRenderer = mc.fontRendererObj

        fun runInformationLines(): List<String> {
            val list: MutableList<String> = mutableListOf()
            if (FMConfig.scoreTotalScore) list.add(getScore(FMConfig.scoreMinimizedName))

            when (FMConfig.scoreSecrets) {
                1 -> list.add(getSecrets(FMConfig.scoreMinimizedName, missing = false))
                2 -> list.add(getSecrets(FMConfig.scoreMinimizedName, missing = true))
            }

            list.add("split")

            if (FMConfig.scoreCrypts) list.add(getCrypts(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreMimic && Location.dungeonFloor.equalsOneOf(6,7)) list.add(getMimic(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreDeaths) list.add(getDeaths(FMConfig.scoreMinimizedName))

            list.add("split1")

            when (FMConfig.scorePuzzles) {
                1 -> list.add(getPuzzles(FMConfig.scoreMinimizedName, false))
                2 -> list.add(getPuzzles(FMConfig.scoreMinimizedName, true))
            }

            return list
        }

        private fun getScore(minimized: Boolean = false): String {
            val scoreColor = when {
                ScoreCalculation.score < 270 -> "§c"
                ScoreCalculation.score < 300 -> "§e"
                else -> "§a"
            }
            var line = if (minimized) "" else "§7Score: "
            line += "$scoreColor${ScoreCalculation.score}"

            return line
        }

        private fun getSecrets(minimized: Boolean = false, missing: Boolean): String {
            var line = if (minimized) "" else "§7Secrets: "
            line += "§b${RunInformation.secretsFound}§7/"
            if (missing) {
                val missingSecrets = (RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)
                line += "§e${missingSecrets}§7/"
            }
            line += "§c${RunInformation.secretTotal}"

            return line
        }

        private fun getCrypts(minimized: Boolean = false): String {
            var line = if (minimized) "§7C: " else "§7Crypts: "
            line += if (RunInformation.cryptsCount >= 5) "§a${RunInformation.cryptsCount}" else "§c${RunInformation.cryptsCount}"
            return line
        }

        private fun getMimic(minimized: Boolean = false): String {
            var line = if (minimized) "§7M: " else "§7Mimic: "
            line += if (RunInformation.mimicKilled) "§a✔" else "§c✘"
            return line
        }

        private fun getDeaths(minimized: Boolean = false): String {
            var line = if (minimized) "§7D: " else "§7Deaths: "
            line += "§c${RunInformation.deathCount}"
            return line
        }

        private fun getPuzzles(minimized: Boolean = false, total: Boolean): String {
            val color = if (RunInformation.completedPuzzles == RunInformation.totalPuzzles) "§a" else "§c"
            var line = if (minimized) "§7P: " else "§7Puzzles: "
            line += "$color${RunInformation.completedPuzzles}"
            if (total) line += "§7/$color${RunInformation.totalPuzzles}"
            return line
        }
    }
}
