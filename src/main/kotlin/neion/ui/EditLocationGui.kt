// https://github.com/Harry282/FunnyMap/tree/master/src/main/kotlin/funnymap/ui
package neion.ui

import neion.ui.GuiRenderer.elements
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

class EditLocationGui : GuiScreen() {

    private var hovered: MovableGuiElement? = null
    private var startOffsetX = 0
    private var startOffsetY = 0
    private var isDragging = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawDefaultBackground()
        elements.forEach { it.draw(mouseX, mouseY) }
        if (!isDragging) hovered = elements.find { it.isHovered(mouseX, mouseY) } else hovered?.setLocation((mouseX - startOffsetX), (mouseY - startOffsetY))
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
                hovered?.let {
                    startOffsetX = (mouseX - it.x)
                    startOffsetY = (mouseY - it.y)
                    isDragging = true
                }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun handleMouseInput() {
        hovered?.mouseScroll(Mouse.getEventDWheel().coerceIn(-1..1))
        super.handleMouseInput()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        hovered?.keyTyped(keyCode)
        super.keyTyped(typedChar, keyCode)
    }
}
