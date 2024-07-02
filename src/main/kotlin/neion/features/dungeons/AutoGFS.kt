package neion.features.dungeons

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.Location.inDungeons
import neion.utils.TextUtils
import neion.utils.Utils.itemID
import neion.utils.Utils.items
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoGFS: Module("Auto EnderPearl GFS", category = Category.DUNGEON, description = "Gets you enderpearls from sack, when you're low on them") {


    private var lastGive = 0L
    val minEPAmount = NumberSetting("Min Amount", default = 5.0, min = 1.0, max = 15.0)

    init {
        addSettings(minEPAmount)
    }

    @SubscribeEvent
    fun onClientTick(e: ClientTickEvent) {
        if (inDungeons) mc.thePlayer?.inventory?.items?.find { it?.itemID == "ENDER_PEARL" }?.let {
                if (System.currentTimeMillis() - lastGive < 5000 || it.stackSize > minEPAmount.value.toInt()) return
                TextUtils.sendCommand("gfs ender_pearl ${16 - it.stackSize}")
                lastGive = System.currentTimeMillis()
            }
    }
}