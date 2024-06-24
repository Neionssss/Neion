package neion.features

import neion.events.CheckRenderEntityEvent
import neion.ui.clickgui.Module
import neion.utils.Utils.equalsOneOf
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HideGrayDamageNumbers: Module("No Gray Damage") {

    @SubscribeEvent
    fun onCheck(e: CheckRenderEntityEvent) {
        if (e.entity.name.equalsOneOf("^§6[\\d,]+$", "^§9[\\d,]+$", "^§2[\\d,]+$", "^§7[\\d,]+\$")) e.isCanceled = true
    }

}