package neion.features

import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting

object HideInventoryEffects: Module("Hide Inv Effects") {
    val onlyOnSkyblock = BooleanSetting("Only on Skyblock")

    init {
        addSettings(onlyOnSkyblock)
    }
}