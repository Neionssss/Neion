package neion.features.dungeons

 import neion.Config
 import neion.Neion
 import neion.utils.Location
 import neion.utils.TextUtils
 import neion.utils.Utils.items
 import net.minecraft.init.Items
 import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object GFS {

    private var lastGive = 0L

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (!Config.autoGFS || !Location.inDungeons) return
        Neion.mc.thePlayer?.inventory?.items?.filter { it?.item == Items.ender_pearl && System.currentTimeMillis() - lastGive > 5000 }?.forEach {
            if (it != null) {
                if (it.stackSize > Config.minep) return
                TextUtils.sendCommand("gfs ender_pearl ${16 - it.stackSize}")
                lastGive = System.currentTimeMillis()
            }
        }
    }
}