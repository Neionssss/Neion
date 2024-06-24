package neion.ui.clickgui.elements

import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.StringSetting
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Provides a text field element.
 *
 * @author Aton
 */
class ElementTextField(parent: ModuleButton, override val setting: StringSetting) : Element(parent,setting) {
    var listening: Boolean

    private val keyBlackList = intArrayOf(
        Keyboard.KEY_LSHIFT,
        Keyboard.KEY_RSHIFT,
        Keyboard.KEY_UP,
        Keyboard.KEY_RIGHT,
        Keyboard.KEY_LEFT,
        Keyboard.KEY_DOWN,
        Keyboard.KEY_END,
        Keyboard.KEY_NUMLOCK,
        Keyboard.KEY_DELETE,
        Keyboard.KEY_LCONTROL,
        Keyboard.KEY_RCONTROL,
        Keyboard.KEY_CAPITAL,
        Keyboard.KEY_LMENU,
        Keyboard.KEY_F1,
        Keyboard.KEY_F2,
        Keyboard.KEY_F3,
        Keyboard.KEY_F4,
        Keyboard.KEY_F5,
        Keyboard.KEY_F6,
        Keyboard.KEY_F7,
        Keyboard.KEY_F8,
        Keyboard.KEY_F9,
        Keyboard.KEY_F10,
        Keyboard.KEY_F11,
        Keyboard.KEY_F12,
        Keyboard.KEY_F13,
        Keyboard.KEY_F14,
        Keyboard.KEY_F15,
        Keyboard.KEY_F16,
        Keyboard.KEY_F17,
        Keyboard.KEY_F18,
        Keyboard.KEY_F19,
        Keyboard.KEY_SCROLL,
        Keyboard.KEY_RMENU,
        Keyboard.KEY_LMETA,
        Keyboard.KEY_RMETA,
        Keyboard.KEY_FUNCTION,
        Keyboard.KEY_PRIOR,
        Keyboard.KEY_NEXT,
        Keyboard.KEY_INSERT,
        Keyboard.KEY_HOME,
        Keyboard.KEY_PAUSE,
        Keyboard.KEY_APPS,
        Keyboard.KEY_POWER,
        Keyboard.KEY_SLEEP,
        Keyboard.KEY_SYSRQ,
        Keyboard.KEY_CLEAR,
        Keyboard.KEY_SECTION,
        Keyboard.KEY_UNLABELED,
        Keyboard.KEY_KANA,
        Keyboard.KEY_CONVERT,
        Keyboard.KEY_NOCONVERT,
        Keyboard.KEY_YEN,
        Keyboard.KEY_CIRCUMFLEX,
        Keyboard.KEY_AT,
        Keyboard.KEY_UNDERLINE,
        Keyboard.KEY_KANJI,
        Keyboard.KEY_STOP,
        Keyboard.KEY_AX,
        Keyboard.KEY_TAB,
    )

    init {
        listening = false
    }

    /**
     * Rendering the element
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val displayValue = setting.text
        val temp = ColorUtil.clickGUIColor
        val color = if (listening) {
            temp
        } else ColorUtil.elementColor

        /** Rendering the Box */
        Gui.drawRect(x, y, (x + width), (y + height), color)


        /** Rendering the text */
        if (TextUtils.getStringWidth(displayValue + "00" + displayName) <= width) {
            TextUtils.drawString(displayName, x + 1, y + 2, -0x1)
            TextUtils.drawString(displayValue, x + width - TextUtils.getStringWidth(displayValue), y + 2, -0x1)
        } else {
            if (isTextHovered(mouseX, mouseY) || listening) {
                TextUtils.drawCenteredStringWithShadow(displayValue, x + width / 2, y + 2, -0x1)
            } else TextUtils.drawCenteredString(displayName, x + width / 2, y + 2, -0x1)
        }
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isTextHovered(mouseX, mouseY)) {
            listening = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Register key strokes.
     */
    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                listening = false
            } else if (keyCode == Keyboard.KEY_BACK) {
                setting.text = setting.text.dropLast(1)
            } else if (!keyBlackList.contains(keyCode)) {
                setting.text += typedChar.toString()
            }
            return true
        }
        return super.keyTyped(typedChar, keyCode)
    }

    /**
     * Checks whether the mouse is hovering the text field
     */
    private fun isTextHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y  && mouseY <= y + height

}