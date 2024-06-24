package neion.features

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils
import neion.utils.TextUtils.startsWithAny
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import java.util.*
import kotlin.concurrent.schedule

object AutoSB: Module("AutoSB", category = Category.GENERAL) {

    val reconnect = BooleanSetting("Auto-Reconnect")

    init {
        addSettings(reconnect)
    }

    @SubscribeEvent
    fun onConnect(e: ClientConnectedToServerEvent?) {
        if (!inSkyblock) Thread {
            while (mc.thePlayer == null) Thread.sleep(300)
            TextUtils.sendCommand("play sb")
        }.start()
    }


    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (!reconnect.enabled || e.type == 2.toByte()) return
        val messagee = e.message.unformattedText
        if (inSkyblock && messagee.startsWith("You were kicked while joining that server!")) {
            TextUtils.info("You have auto SB turned on and will be auto-reconnected in a minute!")
            Timer("ARC", false).schedule(55000) { mc.thePlayer?.sendChatMessage("/play sb") }
        } else if (!inSkyblock && messagee.startsWithAny("You were kicked while joining that server!", "You tried to rejoin too fast, please try again in a moment!")) {
            Timer("ARC", false).schedule(10000) { mc.thePlayer.sendChatMessage("/play sb") }
        }
    }
}