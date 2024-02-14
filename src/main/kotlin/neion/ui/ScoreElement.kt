package neion.ui

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.RunInformation
import neion.funnymap.ScoreCalculation
import neion.utils.Location
import neion.utils.Location.inDungeons
import net.minecraft.client.gui.FontRenderer

class ScoreElement : MovableGuiElement() {
    override var x: Int by FMConfig::scoreX
    override var y: Int by FMConfig::scoreY
    override val h: Int
        get() = fr.FONT_HEIGHT * elementLines
    override val w: Int = fr.getStringWidth("Score: 100/100/100/7 : (300)")
    override var scale: Float by FMConfig::scoreScale
    private var elementLines = 1
        set(value) {
            if (field != value) field = value
        }

    override fun render() {
        var y = 0f
        val lines = getScoreLines()
        elementLines = lines.size
        lines.forEach {
            fr.drawString(it, 0f, y, 0xffffff, true)
            y += fr.FONT_HEIGHT
        }
    }

    override fun shouldRender(): Boolean {
        if (FMConfig.mapShowRunInformation != 2 || !inDungeons) return false
        if (FMConfig.scoreHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }

    companion object {
        val fr: FontRenderer = mc.fontRendererObj

        fun getScoreLines(): List<String> {
            val list: MutableList<String> = mutableListOf()

            when (FMConfig.scoreTotalScore) {
                1 -> list.add(getScore(FMConfig.scoreMinimizedName, false))
                2 -> list.add(getScore(FMConfig.scoreMinimizedName, true))
            }

            when (FMConfig.scoreSecrets) {
                1 -> list.add(getSecrets(FMConfig.scoreMinimizedName, false))
                2 -> list.add(getSecrets(FMConfig.scoreMinimizedName, true))
            }

            if (FMConfig.scoreCrypts) list.add(getCrypts(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreMimic) list.add(getMimic(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreDeaths) list.add(getDeaths(FMConfig.scoreMinimizedName))

            when (FMConfig.scorePuzzles) {
                1 -> list.add(getPuzzles(FMConfig.scoreMinimizedName, false))
                2 -> list.add(getPuzzles(FMConfig.scoreMinimizedName, true))
            }

            return list
        }

        fun runInformationLines(): List<String> {
            val list: MutableList<String> = mutableListOf()

            if (FMConfig.mapShowRunInformation == 1) {
                list.add(getScore(FMConfig.scoreMinimizedName, expanded = false))
            }

            when (FMConfig.scoreSecrets) {
                1 -> list.add(getSecrets(FMConfig.scoreMinimizedName, missing = false))
                2 -> list.add(getSecrets(FMConfig.scoreMinimizedName, missing = true))
            }

            list.add("split")

            if (FMConfig.scoreCrypts) list.add(getCrypts(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreMimic) list.add(getMimic(FMConfig.scoreMinimizedName))
            if (FMConfig.scoreDeaths) list.add(getDeaths(FMConfig.scoreMinimizedName))
            when (FMConfig.scorePuzzles) {
                1 -> list.add(getPuzzles(true, false))
                2 -> list.add(getPuzzles(true, true))
            }

            return list
        }

        private fun getScore(minimized: Boolean = false, expanded: Boolean): String {
            val scoreColor = when {
                ScoreCalculation.score < 270 -> "§c"
                ScoreCalculation.score < 300 -> "§e"
                else -> "§a"
            }
            var line = if (minimized) "" else "§7Score: "
            if (expanded) {
                line += "§b${ScoreCalculation.getSkillScore()}§7/" +
                        "§a${ScoreCalculation.getExplorationScore()}§7/" +
                        "§3${ScoreCalculation.getSpeedScore(RunInformation.timeElapsed)}§7/" +
                        "§d${ScoreCalculation.getBonusScore()} §7: "
            }
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
