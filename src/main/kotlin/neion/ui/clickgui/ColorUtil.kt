package neion.ui.clickgui

import neion.features.ClickGui

/**
 * Provides color for the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
object ColorUtil {
    val clickGUIColor: Int
        get() = ClickGui.color.rgb

    val elementColor: Int
     get() = -0xdcdcdd

    const val textcolor = -0x101011

}