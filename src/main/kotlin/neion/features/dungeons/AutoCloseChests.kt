package neion.features.dungeons

import neion.events.PacketReceiveEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.Location.inDungeons
import neion.utils.Utils.equalsOneOf
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoCloseChests: Module("Auto Close Chests", category = Category.DUNGEON) {

    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        if (!inDungeons) return
        if ((e.packet as? S2DPacketOpenWindow ?: return).windowTitle.unformattedText.equalsOneOf(
                "Chest",
                "Large Chest"
            )
        ) {
            e.isCanceled = true
            mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(e.packet.windowId))
        }
    }
}