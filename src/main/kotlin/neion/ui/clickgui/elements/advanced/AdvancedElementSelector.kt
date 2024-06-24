package neion.ui.clickgui.elements.advanced

import neion.Neion.Companion.mc
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.SelectorSetting
import neion.utils.RenderUtil.tessellator
import neion.utils.RenderUtil.worldRenderer
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import java.awt.Color
import java.util.*

/**
 * Provides a selector element for the advanced gui.
 *
 * @author Aton
 */
class AdvancedElementSelector(parent: AdvancedMenu, module: Module, override val setting: SelectorSetting) : AdvancedElement(parent, module, setting) {


    /**
	 * Renders the element
	 */
    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float) : Int {
        val temp = ColorUtil.clickGUIColor
        val displayValue = setting.selected

        /** Render the box and text */

        val xx = if (TextUtils.getStringWidth(displayValue + "00" + setting.name) <= settingWidth) settingWidth - TextUtils.getStringWidth(displayValue) -6.0 else settingWidth -8.0

        glPushMatrix()
        GlStateManager.translate(xx, 6.5, 1.0)
        if (!comboextended) GlStateManager.rotate(-90f,0f,0f,1f)
        GlStateManager.color(Color.white.red.toFloat(),Color.white.green.toFloat(),Color.white.blue.toFloat())
        mc.textureManager.bindTexture(ResourceLocation("funnymap", "marker.png"))
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(-6.0, 6.0, 0.0).tex(0.0, 0.0).endVertex()
        worldRenderer.pos(6.0, 6.0, 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(6.0, -6.0, 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos(-6.0, -6.0, 0.0).tex(0.0, 1.0).endVertex()
        tessellator.draw()
        glPopMatrix()

        if (TextUtils.getStringWidth(displayValue + "00" + setting.name) <= settingWidth) {
            TextUtils.drawString(setting.name, 1, 2, -0x1)
            TextUtils.drawString(displayValue, settingWidth - TextUtils.getStringWidth(displayValue), 2, -0x1)
        } else {
            if (isButtonHovered(mouseX, mouseY)) {
                TextUtils.drawCenteredStringWithShadow(displayValue, settingWidth / 2, 2, -0x1)
            } else {
                TextUtils.drawCenteredString(setting.name, settingWidth / 2, 2, -0x1)
            }
        }

        Gui.drawRect(0, 13, settingWidth, 15, 0x77000000)
        Gui.drawRect(
            settingWidth / setting.options.size,
            12,
            settingWidth / setting.options.size,
            15,
            temp
        )

        var ay = 15
        if (comboextended) {

            val increment = TextUtils.fontHeight + 2
            for (option in setting.options) {

                Gui.drawRect(0, ay, settingWidth, ay + increment, -0x55ededee)
                val elementtitle = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                TextUtils.drawCenteredString(elementtitle, settingWidth / 2, ay + 2, -0x1)

                /** Highlights the element if it is selected */
                if (option.equals(setting.selected, ignoreCase = true)) Gui.drawRect(x, ay, 2, ay + increment, temp)
                /** Highlights the element when it is hovered */
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y +  ay && mouseY < parent.y + y + ay + increment) Gui.drawRect(
                    settingWidth - 1,
                    ay,
                    settingWidth,
                    ay + increment,
                    temp
                )

                ay += increment
            }
        }
        return ay
    }

    /**
     * Handles interaction with this element.
     * Returns true if interacted with the element to cancel further interactions.
     */
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
            if (isButtonHovered(mouseX, mouseY)) {
                if (mouseButton == 0) {
                    setting.index += 1
                    return true
                } else {
                    comboextended = !comboextended
                    return true
                }
            }

            if (!comboextended) return false
            var ay = y + 15
            val increment = TextUtils.fontHeight + 2
            for (option in setting.options) {
                if (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + ay && mouseY <= parent.y + ay + increment) {
                    setting.selected = option.lowercase(Locale.getDefault())
                    return true
                }
                ay += increment
            }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int) = (mouseX >= parent.x + x && mouseX <= parent.x + x + settingWidth && mouseY >= parent.y + y && mouseY <= parent.y + y + 15)
}