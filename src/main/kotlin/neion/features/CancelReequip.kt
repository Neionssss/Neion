package neion.features

import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting

object CancelReequip: Module("Cancel Reequip") {
    val showWhenChangingSlots = BooleanSetting("Show when changing")
    init {
        addSettings(showWhenChangingSlots)
    }
}