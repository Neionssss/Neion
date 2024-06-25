package neion.features

import neion.ui.clickgui.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoReverse3DView: Module("No Reverse 3D View") {
    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (mc.gameSettings.thirdPersonView == 2) mc.gameSettings.thirdPersonView = 0
    }
}