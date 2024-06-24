package neion.ui.clickgui.elements

import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.settings.Setting
import neion.ui.clickgui.settings.BooleanSetting
import neion.utils.RenderUtil.addQuadVertices
import neion.utils.RenderUtil.bind
import neion.utils.RenderUtil.postDraw
import neion.utils.RenderUtil.preDraw
import neion.utils.RenderUtil.tessellator
import neion.utils.RenderUtil.worldRenderer
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.GL_QUADS
import java.awt.Color

/**
 * Provides a checkbox element.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
class ElementCheckBox(parent: ModuleButton, override var setting: BooleanSetting) : Element(parent = parent, setting = setting) {

    /**
	 * Render the element
	 */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val color = if (setting.enabled) ColorUtil.clickGUIColor else - 0x1000000

        /** Rendering the box */
        Gui.drawRect(x, y, (x + width), (y + height), ColorUtil.elementColor)

        /** Rendering the name and the checkbox */
        TextUtils.drawString(displayName, x + 1, (y + TextUtils.fontHeight / 2 - 0.5).toInt(), -0x1)
        Gui.drawRect((x + width -13), (y + 2), (x + width - 1), (y + 13), color)
        if (isCheckHovered(mouseX, mouseY)) Gui.drawRect((x + width -13),
            (y + 2), (x + width - 1), (y + 13), 0x55111111)
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
    private fun isCheckHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
}