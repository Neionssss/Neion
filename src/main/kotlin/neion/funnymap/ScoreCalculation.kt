package neion.funnymap

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.RunInformation.completedRoomsPercentage
import neion.funnymap.RunInformation.mimicKilled
import neion.funnymap.RunInformation.secretPercentage
import neion.ui.GuiRenderer
import neion.utils.APIHandler
import neion.utils.Location
import neion.utils.TextUtils
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration


// https://i.imgur.com/ifMCejI.png
object ScoreCalculation {
    val paul = APIHandler.hasBonusPaulScore()
        get() = field || FMConfig.paulBonus
    var score = 0
    var message300 = false
        get() = field.also { field = true }
    var message270 = false
        get() = field.also { field = true }

    fun updateScore() {
        score = getSkillScore() + getExplorationScore() + getSpeedScore(RunInformation.timeElapsed) + getBonusScore()
        if (score >= 300 && !message300) {
            message270 = true
            if (FMConfig.scoreMessage != 0) TextUtils.sendPartyChatMessage(FMConfig.message300)
            if (FMConfig.scoreTitle != 0) {
                mc.thePlayer.playSound("random.orb", 1f, 0.5.toFloat())
                GuiRenderer.displayTitle(FMConfig.message300, 40)
            }
            if (FMConfig.timeTo300) TextUtils.info("§3300 Score§7: §a${RunInformation.timeElapsed.toDuration(DurationUnit.SECONDS)}")
        } else if (score >= 270 && !message270) {
            if (FMConfig.scoreMessage == 2) TextUtils.sendPartyChatMessage(FMConfig.message270)
            if (FMConfig.scoreTitle == 2) {
                mc.thePlayer.playSound("random.orb", 1f, 0.5.toFloat())
                GuiRenderer.displayTitle(FMConfig.message270, 40)
            }
        }
    }

    fun getSkillScore(): Int {
        val puzzleDeduction = (RunInformation.totalPuzzles - RunInformation.completedPuzzles) * 10
        val roomPercent = completedRoomsPercentage.coerceAtMost(1f)
        return 20 + ((80 * roomPercent).toInt() - puzzleDeduction - getDeathDeduction()).coerceAtLeast(0)
    }

    fun getDeathDeduction(): Int {
        var deathDeduction = RunInformation.deathCount * 2
        if (FMConfig.scoreAssumeSpirit) deathDeduction -= 1
        return deathDeduction.coerceAtLeast(0)
    }

    fun getExplorationScore(): Int {
        val secretPercent = (secretPercentage / getSecretPercent()).coerceAtMost(1f)
        val roomPercent = completedRoomsPercentage.coerceAtMost(1f)
        return (60 * roomPercent + 40 * secretPercent).toInt()
    }

    fun getSpeedScore(timeElapsed: Int): Int {
        var score = 100
        val limit = getTimeLimit()
        if (timeElapsed < limit) return score
        val percentageOver = (timeElapsed - limit) * 100f / limit
        score -= getSpeedDeduction(percentageOver).toInt()
        return if (Location.dungeonFloor == 0) (score * 0.7).roundToInt() else score
    }

    fun getBonusScore(): Int {
        var score = 0
        score += RunInformation.cryptsCount.coerceAtMost(5)
        if (mimicKilled) score += 2
        if (paul) score += 10
        return score
    }

    fun getSecretPercent(): Float {
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

    private fun getTimeLimit(): Int {
        return if (Location.masterMode) {
            when (Location.dungeonFloor) {
                1, 2, 3, 4, 5 -> 480
                6 -> 600
                else -> 840
            }
        } else {
            when (Location.dungeonFloor) {
                0 -> 1320
                1, 2, 3, 5 -> 600
                4, 6 -> 720
                else -> 840
            }
        }
    }

    /**
     * This is a very ugly function, but it works.
     * The formula on the wiki doesn't seem to work, this variation should never be more than 2 points off.
     */
    private fun getSpeedDeduction(percentage: Float): Float {
        var percentageOver = percentage
        var deduction = 0f

        deduction += (percentageOver.coerceAtMost(20f) / 2f)
        percentageOver -= 20f
        if (percentageOver <= 0) return deduction

        deduction += (percentageOver.coerceAtMost(20f) / 3.5f)
        percentageOver -= 20f
        if (percentageOver <= 0) return deduction

        deduction += (percentageOver.coerceAtMost(10f) / 4f)
        percentageOver -= 10f
        if (percentageOver <= 0) return deduction

        deduction += (percentageOver.coerceAtMost(10f) / 5f)
        percentageOver -= 10f
        if (percentageOver <= 0) return deduction

        deduction += (percentageOver / 6f)
        return deduction
    }
}
