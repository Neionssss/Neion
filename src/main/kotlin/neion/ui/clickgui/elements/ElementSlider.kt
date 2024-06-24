package neion.ui.clickgui.elements

import neion.Neion.Companion.mc
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.roundToInt

/**
 * Provides a slider element.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
class ElementSlider(parent: ModuleButton, override val setting: NumberSetting) : Element(parent,setting) {
    var dragging: Boolean

    init {
        dragging = false
    }

    /**
	 * Renders the element
	 */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val displayval = "" + (setting.value * 100.0).roundToInt() / 100.0
        val hoveredORdragged = isSliderHovered(mouseX, mouseY) || dragging
        val temp = ColorUtil.clickGUIColor

        val percentBar = (setting.value - setting.min) / (setting.max - setting.min)

        /** Render the box */
        Gui.drawRect(x, y, (x + width), (y + height), ColorUtil.elementColor)

        /** Render the text */
        TextUtils.drawString(displayName, x + 1, y + 2, -0x1)
        TextUtils.drawString(displayval, x + width - TextUtils.getStringWidth(displayval), y + 2, -0x1)

        /** Render the slider */
        Gui.drawRect(x, (y + 12), (x + width), (y + 13.5).toInt(), -0xefeff0)
        Gui.drawRect(x, (y + 12), (x + percentBar * width).toInt(), (y + 13.5).toInt(), temp)
        if (percentBar > 0 && percentBar < 1) Gui.drawRect(
            (x + percentBar * width - 1).toInt(),
            (y + 12), (x + (percentBar.toInt() * width).coerceAtMost(width)), (y + 13.5).toInt(), temp
        )


        /** Calculate and set new value when dragging */
        if (dragging) {
            val diff = setting.max - setting.min
            val newVal = setting.min + MathHelper.clamp_double((mouseX - x) / width.toDouble(), 0.0, 1.0) * diff
            setting.value = newVal
        }
    }

    /**
	 * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
	 */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isSliderHovered(mouseX, mouseY)) {
            dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
	 * Stops slider action on mouse release
	 */
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = false
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        val scaledresolution = ScaledResolution(mc)
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = Mouse.getX() * i1 / mc.displayWidth
        val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1

        val scale = 2.0 / mc.gameSettings.guiScale
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        if (isSliderHovered(scaledMouseX, scaledMouseY)){
            if (keyCode == Keyboard.KEY_RIGHT){
                setting.value += setting.increment
                return true
            }
            if (keyCode == Keyboard.KEY_LEFT) {
                setting.value -= setting.increment
                return true
            }
        }
        return super.keyTyped(typedChar, keyCode)
    }

    /**
	 * Checks whether the mouse is hovering the slider
	 */
    private fun isSliderHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y  && mouseY <= y + height
}