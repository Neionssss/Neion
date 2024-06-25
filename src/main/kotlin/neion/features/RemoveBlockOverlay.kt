package neion.features

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.Location.inSkyblock
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RemoveBlockOverlay: Module("No Block Overlay", category = Category.RENDER) {

    @SubscribeEvent
    fun onRenderBlockOverlay(e: RenderBlockOverlayEvent) {
        if (inSkyblock) e.isCanceled = true
    }
}