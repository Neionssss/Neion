package neion.ui.clickgui.elements

import neion.ui.clickgui.settings.Setting
import neion.utils.TextUtils

/**
 * Parent class to the settings elements in the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
open class Element(var parent: ModuleButton?, open val setting: Setting? = null, var displayName: String? = null) {
    var offset = 0
    var x = 0
    var y = 0
    var width = 0
    var height = 0
    var comboextended = false

    fun update() {
        /** Positioning the Element. Offset is handled in ClickGUI to prevent overlap */
        x = parent!!.x
        y = parent!!.y + offset
        width = parent!!.width
        height = 15

        /** Determine the title and expand box if needed */
        val name = setting?.name ?: if (this is ElementKeyBind) "Key Bind" else return
        displayName = name
        height = when (this) {
            is ElementSelector -> if (comboextended) (setting.options.size * (TextUtils.fontHeight + 2) + 15) else 15
            is ElementColor -> if (comboextended) if (setting.allowAlpha) 15 * 5 else 15 * 4 else 15
            is ElementTextField -> 12
            is ElementKeyBind -> 11
            is ElementAction -> 11
            else -> height
        }
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) = isHovered(mouseX, mouseY)

    /**
     * Overridden in the elements to enable key detection. Returns true when an action was taken.
     */
    open fun keyTyped(typedChar: Char, keyCode: Int) = false

    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
    private fun isHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
}