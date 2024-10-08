package neion.ui

import neion.Neion.Companion.modDir
import neion.features.CustomGUI
import neion.features.CustomScoreboard
import neion.features.dungeons.ChestProfit
import neion.ui.*
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.ModuleConfig
import neion.utils.RenderUtil
import neion.utils.TextUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException

/**
 * The GUI for editing the positions and scale of HUD elements.
 *
 * @author Aton
 */
object EditHudGUI : GuiScreen() {

    val hudElements: ArrayList<HudElement> = arrayListOf(
        Mapping.MapElement(),
        ChestProfit.ChestProfitElement,
        CustomGUI.DungeonSecretDisplay,
        CustomGUI.ClearedDisplay,
        CustomGUI.TimeDisplay,
        CustomGUI.ManaDisplay,
        CustomGUI.HealthDisplay,
        CustomScoreboard.ScoreboardElement
    )
    private var draggingElement: HudElement? = null
    private var startOffsetX = 0
    private var startOffsetY = 0

    /**
     * Draw a previews of all hud elements, regardless of whether they are visible.
     */
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        // Render a reset Button
        renderResetButton(mouseX, mouseY)

        for (element in hudElements) element.renderPreview()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun renderResetButton(mouseX: Int, mouseY: Int) {
        val resetText = "Reset HUD"
        val scaledResolution = ScaledResolution(mc)

        GL11.glPushMatrix()
        GL11.glTranslated(
            scaledResolution.scaledWidth.toDouble() / 2.0,
            scaledResolution.scaledHeight.toDouble(),
            0.0
        )
        // Note: if you change these values they also have to be changed in isCursorOnReset
        val textWidth = TextUtils.getStringWidth(resetText)
        val textHeight = TextUtils.fontHeight.toDouble()
        val textX = -textWidth/2.0
        val textY = -textHeight -25
        val boxX = textX -20
        val boxY = textY -5
        val boxHeight = textHeight + 10
        val boxWidth = textWidth + 40.0

        val buttonColor = if (isCursorOnReset(mouseX, mouseY)) {
            Color(-0x00000000, false)
        } else {
            Color(-0x44eaeaeb, true).darker()
        }
        RenderUtil.renderRect(boxX, boxY, boxWidth, boxHeight, buttonColor)

        TextUtils.drawString(resetText, textX.toInt(), textY.toInt(), ColorUtil.clickGUIColor)
        GL11.glPopMatrix()
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val mouseX = Mouse.getEventX() * super.width / super.mc.displayWidth
        val mouseY = super.height - Mouse.getEventY() * super.height / super.mc.displayHeight - 1

        //Scaling mouse coords neccessary here if the gui scale is changed

        var i = Mouse.getEventDWheel()
        if (i != 0) {
            if (i > 1) i = 1
            if (i < -1) i = -1
            if (isShiftKeyDown()) i *= 7
            /** Check all hud elements for scroll action. this is used to change the scale
             * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
            for (element in hudElements.reversed()) {
                if (isCursorOnElement(mouseX, mouseY, element)) element.scroll(i)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (isCursorOnReset(mouseX, mouseY)) {
                for (element in hudElements.reversed()) element.resetElement()
            } else
                for (element in hudElements.reversed()) {
                    if (isCursorOnElement(mouseX, mouseY, element)) {
                        draggingElement = element
                        startOffsetX = mouseX - element.x
                        startOffsetY = mouseY - element.y
                        break
                    }
                }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (clickedMouseButton == 0 && draggingElement != null) {
            draggingElement!!.x = mouseX - startOffsetX
            draggingElement!!.y = mouseY - startOffsetY
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingElement = null
        ModuleConfig(modDir).saveConfig()
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        ModuleConfig(modDir).saveConfig()
    }

    private fun isCursorOnElement(mouseX: Int, mouseY: Int, element: HudElement): Boolean {
        return mouseX > element.x && mouseX < (element.x + element.width * element.scale) && mouseY > element.y && mouseY< (element.y + element.height * element.scale)
    }

    private fun isCursorOnReset(mouseX: Int, mouseY: Int) : Boolean {
        val resetText = "Reset HUD"
        val scaledResolution = ScaledResolution(mc)
        // Note: if you change these values they also have to be changed in isCursorOnReset
        val textWidth = TextUtils.getStringWidth(resetText)
        val textHeight = TextUtils.fontHeight.toDouble()
        val textX = -textWidth/2.0 + scaledResolution.scaledWidth.toDouble() / 2.0
        val textY = -textHeight -25 + scaledResolution.scaledHeight.toDouble()
        val boxX = textX -20
        val boxY = textY -5
        val boxHeight = textHeight + 10
        val boxWidth = textWidth + 40.0
        return mouseX >= boxX && mouseX < (boxX + boxWidth) && mouseY >= boxY && mouseY < (boxY + boxHeight)
    }
}