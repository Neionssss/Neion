package neion.features

import neion.ui.HudElement
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.gui.ScaledResolution


object CustomScoreboard: Module("Custom Scoreboard", category = Category.RENDER) {

    val settingX = NumberSetting("X", default = ScaledResolution(mc).scaledWidth_double, hidden = true)
    val settingY = NumberSetting("Y", default = ScaledResolution(mc).scaledHeight_double, hidden = true)
    val scale = NumberSetting("Scale", default = 1.0, hidden = true)

    init {
        addSettings(settingY, settingX, scale)
    }


    object ScoreboardElement : HudElement(settingX, settingY, 100, 100, scale) {

        override fun shouldRender() = enabled && Location.inSkyblock

        override fun render() {
            Location.getLines().reversed().run {
                if (isEmpty()) return
                val width = maxBy { it.length }
                val height = size * 10
                val index2 = indexOf(find { it.contains( "Objective") })
                filter { !it.contains("hypixel") && !it.equalsOneOf(getOrNull(index2) ?: "", getOrNull(index2 + 1) ?: "", getOrNull(index2 - 1) ?: "") }.forEachIndexed { index, text ->
                    RenderUtil.renderText(text, 0, 10 * index)
                }
            }
        }
    }
}