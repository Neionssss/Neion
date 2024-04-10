// https://github.com/Harry282/FunnyMap/tree/master/src/main/kotlin/funnymap/ui

package neion.ui

import neion.Neion.Companion.mc
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard

open class MovableGuiElement(var name: String) {

    val x
        get() = GuiRenderer.positions[name]?.first ?: (GuiRenderer.elements.indexOf(this) * 10)
    val y
        get() = GuiRenderer.positions[name]?.second ?: (GuiRenderer.elements.indexOf(this) * 10)
    open val w = 0
    open val h = mc.fontRendererObj.FONT_HEIGHT
    val scale
        get() = GuiRenderer.positions[name]?.third ?: 1f

    open fun render() {}

    open fun shouldRender() = true

    fun isHovered(mouseX: Int, mouseY: Int) = mouseX in x..(x + w * scale).toInt() && mouseY in y..(y + h * scale).toInt()

    fun setLocation(x: Int, y: Int) {
        GuiRenderer.positions[name] = Triple(x.coerceIn(0, (mc.displayWidth - w * scale).toInt()),y.coerceIn(0, (mc.displayHeight - h * scale).toInt()),scale)
    }

    fun mouseScroll(direction: Int) {
        if (direction != 0) {
            var increment = direction * 0.01f
            if (!GuiScreen.isShiftKeyDown()) increment *= 5
            GuiRenderer.positions[name] = Triple(x,y,(scale + increment).coerceAtLeast(0.1f))
        }
        EditLocationGui.save()
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
        EditLocationGui.save()
    }

}