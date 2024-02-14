// https://github.com/Harry282/FunnyMap/tree/master/src/main/kotlin/funnymap/ui

package neion.ui

import neion.Neion.Companion.mc
import neion.utils.RenderUtil
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GuiRenderer {
    val elements = mutableListOf(
        ScoreElement(),
        MapElement,
        ChestProfitElement,
        DungeonSecretDisplay,
        ClearedDisplay,
        TimeDisplay,
        ManaDisplay
    )
    private var displayTitle = ""
    var titleTicks = 0

    fun displayTitle(title: String, ticks: Int) {
        displayTitle = title
        titleTicks = ticks
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        if (mc.currentScreen is EditLocationGui) return

        mc.entityRenderer.setupOverlayRendering()

        elements.forEach {
            if (!it.shouldRender()) return@forEach
            GlStateManager.pushMatrix()
            GlStateManager.translate(it.x.toFloat(), it.y.toFloat(), 0f)
            GlStateManager.scale(it.scale, it.scale, 1f)
            it.render()
            GlStateManager.popMatrix()
        }

        if (titleTicks > 0) {
            val sr = ScaledResolution(mc)
            RenderUtil.renderText(
                displayTitle,
                sr.scaledWidth / 2,
                sr.scaledHeight / 4,
                4.0)
        }
    }
}