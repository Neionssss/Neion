package neion.ui.clickgui.elements

import neion.ui.clickgui.Module
import neion.ui.clickgui.ColorUtil
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color

/**
 * Provides a key bind element.
 *
 * @author Aton
 */
class ElementKeyBind(parent: ModuleButton, val mod: Module) : Element(parent = parent) {

    var listening = false

    private val keyBlackList = intArrayOf()


    /**
     * Render the element
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val keyName = if(mod.keyCode > 0) Keyboard.getKeyName(mod.keyCode) ?: "Err"
        else if (mod.keyCode < 0) Mouse.getButtonName(mod.keyCode + 100) else ".."
        val displayValue = "[$keyName]"
        val color = if (listening) {
            ColorUtil.clickGUIColor
        } else ColorUtil.elementColor

        /** Rendering the box */
        Gui.drawRect(x, y, (x + width), (y + height), color)

        /** Title und Checkbox render. */
        TextUtils.drawString(displayName, x + 1, y + 2, -0x1)
        TextUtils.drawString(displayValue, x + width - TextUtils.getStringWidth(displayValue), y + 2, -0x1)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isCheckHovered(mouseX, mouseY) ) {
            listening = !listening
            return true
        } else if (listening) {
            mod.keyCode = -100 + mouseButton
            listening = false
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Register keystrokes. Used to set the key bind.
     */
    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK) {
                mod.keyCode = Keyboard.KEY_NONE
                listening = false
            } else if (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                listening = false
            } else if (!keyBlackList.contains(keyCode)) {
                mod.keyCode = keyCode
                listening = false
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    /**
     * Checks whether this element is hovered
     */
    private fun isCheckHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y  && mouseY <= y + height
}