package neion.ui.clickgui.elements

import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.ActionSetting
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui

/**
 * Provides the Menu Button for action settings.
 *
 * @author Aton
 */
class ElementAction(parent: ModuleButton, override val setting: ActionSetting) : Element(parent = parent,setting = setting, displayName = setting.name)  {

    /**
     * Render the element
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val color = ColorUtil.elementColor

        /** Rendering the box */
        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color)

        /** Title und Checkbox render. */
        TextUtils.drawString(displayName, x + 1, y + 2, -0x1)
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed.
     * Used to activate the elements action.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isButtonHovered(mouseX, mouseY) ) {
            (setting as? ActionSetting)?.action?.let { it() }
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y  && mouseY <= y + height
}