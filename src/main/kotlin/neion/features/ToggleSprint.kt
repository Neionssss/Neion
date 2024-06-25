package neion.features

import neion.ui.clickgui.Module
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object ToggleSprint: Module("Auto-Sprint") {

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
    }
}