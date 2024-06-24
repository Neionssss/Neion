package neion.ui.clickgui.elements.advanced

import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.ColorUtil.textcolor
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui

/**
 * Provides a checkbox element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementCheckBox(parent: AdvancedMenu, module: Module, override val setting: BooleanSetting) : AdvancedElement(parent, module, setting) {


    /**
     * Render the element
     */
    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float) : Int{
        val temp = ColorUtil.clickGUIColor

        /** Rendering the name and the checkbox */
        TextUtils.drawString(
            setting.name, 1,
            2, textcolor
        )
        Gui.drawRect(
            (settingWidth - 13), 2, settingWidth - 1, 13,
            if (setting.enabled) temp else -0x1000000
        )
        if (isCheckHovered(mouseX, mouseY)) Gui.drawRect(
            settingWidth - 13,  2, settingWidth -1,
            13, 0x55111111
        )
        return settingHeight
    }

    /**
     * Handles mouse clicks for this element and returns true if an action was performed
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered(mouseX, mouseY)) {
            setting.toggle()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isCheckHovered(mouseX: Int, mouseY: Int) = mouseX >= parent.x + x + settingWidth - 13 && mouseX <= parent.x + x + settingWidth - 1 && mouseY >= parent.y + y + 2 && mouseY <= parent.y + y + settingHeight - 2
}