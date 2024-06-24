package neion.ui.clickgui.elements

import neion.Neion.Companion.mc
import neion.features.ClickGui
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.SelectorSetting
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import java.util.*

/**
 * Provides a selector element.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
class ElementSelector(parent: ModuleButton, override val setting: SelectorSetting) : Element(parent,setting) {


    /**
	 * Renders the element
	 */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val temp = ColorUtil.clickGUIColor
        val displayValue = setting.selected

        /** Render the box and text */
        if(parent?.parent?.shouldRender(y + 15) == true) {
            Gui.drawRect(x, y, (x + width), (y + height), ColorUtil.elementColor)
            if (TextUtils.getStringWidth(displayValue + "00" + displayName) <= width) {
                TextUtils.drawString(displayName, x + 1, y + 2, -0x1)
                TextUtils.drawString(displayValue, x + width - TextUtils.getStringWidth(displayValue), y + 2, -0x1)
            } else {
                if (isButtonHovered(mouseX, mouseY)) {
                    TextUtils.drawCenteredStringWithShadow(displayValue, x + width / 2, y + 2, -0x1)
                } else TextUtils.drawCenteredString(displayName, x + width / 2, y + 2, -0x1)
            }

            Gui.drawRect(x, (y + 13), (x + width), (y + 15), 0x77000000)
            Gui.drawRect(
                (x + width * 0.4).toInt(),
                (y + 12),
                (x + width * 0.6).toInt(),
                (y + 15),
                temp
            )
        }

        if (comboextended) {
            var ay = y + 15
            val increment = TextUtils.fontHeight + 2
            for (option in setting.options) {
                if(parent?.parent?.shouldRender(ay + increment) == true) {
                    Gui.drawRect(x, ay, (x + width), (ay + increment), -0x55ededee)
                    val elementtitle = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                    TextUtils.drawCenteredString(elementtitle, x + width / 2, ay + 2, -0x1)

                    /** Highlights the element if it is selected */
                    if (option.equals(setting.selected, ignoreCase = true)) {
                        Gui.drawRect(
                            x,
                            ay,
                            (x + 1.5).toInt(),
                            (ay + increment),
                            temp
                        )
                    }
                    /** Highlights the element when it is hovered */
                    if (mouseX >= x && mouseX <= x + width && mouseY >= ay && mouseY < ay + increment) {
                        Gui.drawRect(
                            (x + width - 1.2).toInt(),
                            ay,
                            (x + width),
                            (ay + increment),
                            temp
                        )
                    }
                }
                ay += increment
            }
        }
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered(mouseX, mouseY)) {
                setting.index += 1
                return true
            }

            if (!comboextended) return false
            var ay = y + 15
            val increment = TextUtils.fontHeight + 2
            for (option in setting.options) {
                if(parent?.parent?.shouldRender(ay + increment) == true) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= ay && mouseY <= ay + increment) {
                        if (parent?.parent?.clickgui != null) setting.selected = option.lowercase(Locale.getDefault())
                        return true
                    }
                }
                ay += increment
            }
        } else if (mouseButton == 1) {
            if (isButtonHovered(mouseX, mouseY)) {
                comboextended = !comboextended
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int) = (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 15) && parent?.parent?.shouldRender(y+15) ?: false
}