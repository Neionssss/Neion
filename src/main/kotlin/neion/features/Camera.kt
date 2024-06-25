package neion.features

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.NumberSetting

object Camera: Module("Custom Camera", category = Category.RENDER) {

    val clip = BooleanSetting("Camera clip")
    val distance = NumberSetting("Camera Distance", min = 1.0, default = 4.0, max = 150.0)

    init {
        addSettings(clip, distance)
    }
}