package neion.ui

import neion.funnymap.RunInformation
import neion.funnymap.ScoreCalculation.score
import neion.funnymap.map.Puzzle
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.elements.advanced.AdvancedMenu
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.SelectorSetting
import neion.utils.Location
import neion.utils.Utils.equalsOneOf

object Score: Module("Score", category = Category.MAP) {

    private val scoreMinimizedName = BooleanSetting("Minimized Text")
    private val scoreSecrets = SelectorSetting(
        "Secrets type",
        default = "Total and Missing",
        options = arrayOf("OFF", "Total", "Total and Missing")
    )
    val showRunInfo = BooleanSetting("Show Run Info", enabled = true)
    val scorePuzzles = SelectorSetting("Puzzles type", default = "OFF", options = arrayOf("OFF", "Total", "Completed and Total"))
    val scoreCrypts = BooleanSetting("Crypts Info")
    val scoreMimic = BooleanSetting("Mimic Info")
    val scoreDeath = BooleanSetting("Death Info")

    init {
        addSettings(
            showRunInfo,
            scoreMinimizedName,
            scoreSecrets,
            scorePuzzles,
            scoreCrypts,
            scoreMimic,
            scoreDeath,
        )
    }

    override fun keyBind() {
        onEnable()
    }

    override fun onEnable() {
        AdvancedMenu.openScore = true
    }

    override fun onDisable() {}

    val lines: List<String>
        get() {
            val list: MutableList<String> = mutableListOf()
            list.add(getScore())

            if (scoreSecrets.selected != "OFF") list.add(getSecrets())

            list.add("split")

            if (scoreCrypts.enabled) list.add(getCrypts())
            if (scoreMimic.enabled && Location.dungeonFloor.equalsOneOf(6, 7)) list.add(getMimic())
            if (scoreDeath.enabled) list.add(getDeaths())

            list.add("split1")

            if (scorePuzzles.selected != "OFF") list.add(getPuzzles())

            return list
        }

    private fun getScore(): String {
        val scoreColor = when {
            score < 270 -> "§c"
            score < 300 -> "§e"
            else -> "§a"
        }
        var line = if (scoreMinimizedName.enabled) "" else "§7Score: "
        line += "$scoreColor${score}"

        return line
    }

    private fun getSecrets(): String {
        var line = if (scoreMinimizedName.enabled) "" else "§7Secrets: "
        line += "§b${RunInformation.secretsFound}§7/"
        if (scoreSecrets.selected == "Total and Missing") {
            val missingSecrets = (RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)
            line += "§e${missingSecrets}§7/"
        }
        line += "§c${RunInformation.secretTotal}"

        return line
    }

    private fun getCrypts(): String {
        var line = if (scoreMinimizedName.enabled) "§7C: " else "§7Crypts: "
        line += if (RunInformation.cryptsCount >= 5) "§a${RunInformation.cryptsCount}" else "§c${RunInformation.cryptsCount}"
        return line
    }

    private fun getMimic(): String {
        var line = if (scoreMinimizedName.enabled) "§7M: " else "§7Mimic: "
        line += if (RunInformation.mimicKilled) "§a✔" else "§c✘"
        return line
    }

    private fun getDeaths(): String {
        var line = if (scoreMinimizedName.enabled) "§7D: " else "§7Deaths: "
        line += "§c${RunInformation.deathCount}"
        return line
    }

    private fun getPuzzles(): String {
        val color = if (RunInformation.completedPuzzles == RunInformation.totalPuzzles) "§a" else "§c"
        var line = if (scoreMinimizedName.enabled) "§7P: " else "§7Puzzles: "
        line += "$color${RunInformation.completedPuzzles}"
        if (scorePuzzles.selected == "Completed and Total") line += "§7/$color${RunInformation.totalPuzzles}"
        line += if (Puzzle.entries.count { it.completed == false } == 0) "§a✔" else "§c✘"
        return line
    }
}
