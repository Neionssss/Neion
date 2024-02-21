package neion.funnymap

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.ui.GuiRenderer
import neion.utils.APIHandler
import neion.utils.Location
import neion.utils.TextUtils
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
        val roomPercent = RunInformation.completedRoomsPercentage.coerceAtMost(1f)
        val explore = (60 * roomPercent + 40 * (RunInformation.secretPercentage / getSecretPercent()).coerceAtMost(1f)).toInt()
        val skill = 20 + ((80 * roomPercent).toInt() - (RunInformation.totalPuzzles - RunInformation.completedPuzzles) * 10 - getDeathDeduction()).coerceAtLeast(0)
        val limit = if (Location.masterMode) {
            when (Location.dungeonFloor) {
                1, 2, 3, 4, 5 -> 480
                6 -> 600
                else -> 840
            }
        } else when (Location.dungeonFloor) {
            0 -> 1320
            1, 2, 3, 5 -> 600
            4, 6 -> 720
            else -> 840
        }
            var percentageOver = RunInformation.timeElapsed - limit * 100 / limit
            var deduction = 0f

            if (RunInformation.timeElapsed > limit) {
                deduction += (percentageOver.coerceAtMost(20) / 2)
                percentageOver -= 20

                deduction += (percentageOver.coerceAtMost(20) / 3.5f)
                percentageOver -= 20

                deduction += (percentageOver.coerceAtMost(10) / 4)
                percentageOver -= 10

                deduction += (percentageOver.coerceAtMost(10) / 5)
                percentageOver -= 10

                deduction += (percentageOver / 6)
            }
        score = skill + explore + (100 - deduction.toInt()) + getBonusScore()
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



    fun getDeathDeduction(): Int {
        var deathDeduction = RunInformation.deathCount * 2
        if (FMConfig.scoreAssumeSpirit) deathDeduction -= 1
        return deathDeduction.coerceAtLeast(0)
    }

    fun getBonusScore(): Int {
        var score = 0
        score += RunInformation.cryptsCount.coerceAtMost(5)
        if (RunInformation.mimicKilled) score += 2
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
}
