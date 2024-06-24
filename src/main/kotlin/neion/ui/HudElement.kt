package neion.ui

import neion.Neion.Companion.mc
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.RenderUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.common.MinecraftForge
import java.awt.Color

/**
 * Provides functionality for game overlay elements.
 * @author Aton
 */
abstract class HudElement
/**
 * It is advised to use the other constructor unless this one is required.
 */(val xSett: NumberSetting, val ySett: NumberSetting, var width: Int = 10, var height: Int = 10, val scaleSett: NumberSetting) {


    private val zoomIncrement = 0.05

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    /**
     * Use these instead of a direct reference to the NumberSetting
     */
    var x: Int
        get() = xSett.value.toInt()
        set(value) {
            xSett.value = value.toDouble()
        }

    var y: Int
        get() = ySett.value.toInt()
        set(value) {
            ySett.value = value.toDouble()
        }
    var scale: Double
        get() = scaleSett.value
        set(value) {
            scaleSett.value = value
        }


    /**
     * Resets the position of this hud element by setting the value of xSett and ySett to their default.
     *
     * Can be overridden in the implementation.
     */
    open fun resetElement() {
        xSett.value = xSett.default
        ySett.value = ySett.default
        scaleSett.value = scaleSett.default
    }

    /**
     * Handles scroll wheel action for this element.
     * Can be overridden in implementation.
     */
    open fun scroll(amount: Int) {
        scale += amount * zoomIncrement
    }


    abstract fun shouldRender(): Boolean

    /**
     * Override this method in your implementations.
     *
     * This method is responsible for rendering the HUD element.
     * Within this method coordinates are already transformed regarding the HUD position [x],[x] and [scale].
     */
    abstract fun render()

    /**
     * Used for moving the hud element.
     * Draws a rectangle in place of the actual element
     */
    fun renderPreview() {
        mc.entityRenderer.setupOverlayRendering()
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(scale, scale, 1.0)

        render()
        RenderUtil.renderRectBorder(
            0.0,
            0.0,
            width.toDouble(),
            height.toDouble(),
            2.0,
            Color.white
        )

        GlStateManager.popMatrix()
    }
}