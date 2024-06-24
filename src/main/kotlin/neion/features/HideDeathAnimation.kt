package neion.features

import neion.ui.clickgui.Module
import neion.utils.Location.inSkyblock
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HideDeathAnimation: Module("No Death Animation") {

    @SubscribeEvent
    fun onLivingDeath(e: LivingDeathEvent) {
        if (inSkyblock) e.entity.setDead()
    }
}