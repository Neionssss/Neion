// https://github.com/Harry282/FunnyMap/tree/master/src/main/kotlin/funnymap/ui

package neion.ui

import neion.Neion.Companion.mc
import neion.utils.RenderUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import java.awt.Color

abstract class MovableGuiElement {

    abstract var x: Int
    abstract var y: Int
    abstract val w: Int
    abstract val h: Int
    open var scale: Float = 1f

    open fun draw(mouseX: Int, mouseY: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(scale, scale, 1f)
        render()
        GlStateManager.popMatrix()

        RenderUtil.renderRectBorder(
            x.toDouble(),
            y.toDouble(),
            w * scale.toDouble(),
            h * scale.toDouble(),
            0.5,
            Color(255, 255, 255))
    }

    open fun render() {}

    open fun shouldRender(): Boolean = true

    fun isHovered(mouseX: Int, mouseY: Int) = mouseX in x..(x + w * scale).toInt() && mouseY in y..(y + h * scale).toInt()

    fun setLocation(x: Int, y: Int) {
        this.x = x.coerceIn(0, ((mc.displayWidth - w * scale).toInt()))
        this.y = y.coerceIn(0, (mc.displayHeight - h * scale).toInt())
    }

    fun mouseScroll(direction: Int) {
        if (direction != 0) {
            var increment = direction * 0.01f
            if (!GuiScreen.isShiftKeyDown()) increment *= 5
            scale = (scale + increment).coerceAtLeast(0.1f)
        }
    }

    fun keyTyped(keyCode: Int) {
        val increment = if (GuiScreen.isShiftKeyDown()) 5 else 1
        Keyboard.enableRepeatEvents(true)
        when (keyCode) {
            Keyboard.KEY_LEFT -> setLocation(x - increment, y)
            Keyboard.KEY_RIGHT -> setLocation(x + increment, y)
            Keyboard.KEY_UP -> setLocation(x, y - increment)
            Keyboard.KEY_DOWN -> setLocation(x, y + increment)
        }
    }
}