package neion.ui.clickgui.elements

import neion.Neion.Companion.mc
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.ColorSetting
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Provides a color selector element.
 * Based on HeroCode's gui.
 *
 * @author Aton
 */
class ElementColor(parent: ModuleButton, override var setting: ColorSetting) : Element(parent = parent, setting = setting) {
    var dragging: Int?

    private val hueScale = ResourceLocation("funnymap", "default/hueColor.png")

    init {
        dragging = null
    }

    /**
     * Renders the element
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val temp = ColorUtil.clickGUIColor

        val colorValue = setting.value.rgb

        /** Render the box and text */
        if(parent?.parent?.shouldRender(y + 15) == true) {
            Gui.drawRect(x, y, (x + width), (y + height), ColorUtil.elementColor)
            TextUtils.drawString(displayName, x + 1, y + 2, -0x1)

            // Render the color preview
            Gui.drawRect(
                (x + width - 26),
                (y + 2),
                (x + width - 1),
                (y + 11),
                colorValue
            )

            // Render the tab indicating the dropdown
            Gui.drawRect(x, (y + 13), (x + width), (y + 15), 0x77000000)
            Gui.drawRect(
                (x + width * 0.4).toInt(),
                (y + 12),
                (x + width * 0.6).toInt(),
                (y + 15),
                temp
            )
        }

        // Render the extended
        if (comboextended) {
            val startY = parent?.parent?.validStart(y + 15) ?: (y + 15)
            Gui.drawRect(x, (startY), (x + width), (y + height), -0x55ededee)
            var ay = y + 15
            val increment = 15

            // Render the color sliders
            for (currentColor in setting.colors()) {
                if(parent?.parent?.shouldRender(ay + increment) == true) {
                    // If hue, render the hue bar.
                    if (currentColor == ColorSetting.ColorComponent.HUE) {
                        hueScale.let {
                            GL11.glPushMatrix()
                            GlStateManager.color(255f, 255f, 255f, 255f)
                            mc.textureManager.bindTexture(it)
                            Gui.drawModalRectWithCustomSizedTexture(
                                x,
                                ay,
                                0f, 0f, width, 11, width.toFloat(), 11.toFloat()
                            )
                            GL11.glPopMatrix()
                        }
                    }

                    val dispVal = "" + (setting.getNumber(currentColor) * 100.0).roundToInt() / 100.0
                    TextUtils.drawString(currentColor.getName(), x + 1, ay + 2, -0x1)
                    TextUtils.drawString(dispVal, x + width - TextUtils.getStringWidth(dispVal), ay + 2, -0x1)

                    val maxVal = currentColor.maxValue()
                    val percentage = setting.getNumber(currentColor) / maxVal
                    Gui.drawRect(x, (ay + 12), (x + width), (ay + 13.5).toInt(), -0xefeff0)
                    Gui.drawRect(x, (ay + 12), (x + percentage * width).toInt(), (ay + 13.5).toInt(), temp)

                    /** Calculate and set new value when dragging */
                    if (dragging == currentColor.ordinal) {
                        val newVal = MathHelper.clamp_double((mouseX - x) / width.toDouble(), 0.0, 1.0) * maxVal
                        setting.setNumber(currentColor, newVal)
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
                // for now also extend on left click
                comboextended = !comboextended
                return true
            }

            if (!comboextended) return false
            var ay = y + 15
            val increment = 15
            for (currentColor in setting.colors()) {
                if(parent?.parent?.shouldRender(ay + increment) == true) {
                    if (mouseX >= x && mouseX <= x + width && mouseY >= ay && mouseY <= ay + increment) {
                        dragging = currentColor.ordinal
                        return true
                    }
                }
                ay += 15
            }
        } else if(mouseButton == 1) {
            if (isButtonHovered(mouseX, mouseY)) {
                comboextended = !comboextended
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Stops slider action on mouse release
     */
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
    }

    /**
     * Check for arrow keys to move the slider by one increment.
     */
    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!comboextended) return false
        val scaledresolution = ScaledResolution(mc)
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = Mouse.getX() * i1 / mc.displayWidth
        val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1
        val scale = 2.0 / mc.gameSettings.guiScale
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        var ay = y + 15
        val increment = 15
        for (currentColor in setting.colors()) {
            if(parent?.parent?.shouldRender(ay + increment) == true) {
                if (scaledMouseX >= x && scaledMouseX <= x + width && scaledMouseY >= ay && scaledMouseY <= ay + increment) {
                    if (keyCode == Keyboard.KEY_RIGHT) {
                        setting.setNumber(currentColor, setting.getNumber(currentColor)+currentColor.maxValue()/255.0)
                    }
                    if (keyCode == Keyboard.KEY_LEFT) {
                        setting.setNumber(currentColor, setting.getNumber(currentColor)-currentColor.maxValue()/255.0)
                    }
                    return true
                }
            }
            ay += 15
        }
        return super.keyTyped(typedChar, keyCode)
    }


    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 15 && parent?.parent?.shouldRender(y+15) ?: false
}