package neion.funnymap

import neion.features.ForcePaul
import neion.funnymap.RunInformation.completedRoomsPercentage
import neion.funnymap.RunInformation.cryptsCount
import neion.funnymap.RunInformation.deathCount
import neion.funnymap.RunInformation.firstDeathSpirit
import neion.funnymap.RunInformation.mimicKilled
import neion.funnymap.RunInformation.secretPercentage
import neion.utils.APIHandler
import neion.utils.Location

object ScoreCalculation {
    val paul = APIHandler.hasBonusPaulScore()
        get() = field || ForcePaul.enabled
    var score = 0

    // https://i.imgur.com/ifMCejI.png
    fun updateScore() {
        val roomPercent = completedRoomsPercentage.coerceAtMost(1f)
        val explore = (60 * roomPercent + 40 * (secretPercentage / getSecretPercent()).coerceAtMost(1f)).toInt()
        val skill = 20 + ((80 * roomPercent).toInt() - (RunInformation.totalPuzzles - RunInformation.completedPuzzles) * 10 - getDeathDeduction()).coerceAtLeast(0)
        score = skill + explore + 100 + getBonusScore()
    }


    fun getDeathDeduction(): Int {
        var deathDeduction = deathCount * 2
        if (firstDeathSpirit && deathCount > 0) deathDeduction -= 1
        return deathDeduction.coerceAtLeast(0)
    }

    fun getBonusScore(): Int {
        var score = 0
        score += cryptsCount.coerceAtMost(5)
        if (mimicKilled) score += 2
        if (paul) score += 10
        return score
    }

    fun getSecretPercent() = if (Location.masterMode) 1f else when (Location.dungeonFloor) {
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