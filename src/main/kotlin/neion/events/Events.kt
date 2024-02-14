package neion.events

import neion.utils.TextUtils.stripControlCodes
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.Cancelable

open class ClickEvent : DebugEvent() {
        @Cancelable
        class LeftClickEvent : ClickEvent()

        @Cancelable
        class RightClickEvent : ClickEvent()

        @Cancelable
        class MiddleClickEvent : ClickEvent()
    }

class Render3DEvent : DebugEvent()

class ChatEvent(val packet: S02PacketChat) : DebugEvent() {
    val text: String by lazy { packet.chatComponent.unformattedText.stripControlCodes() }
}

@Cancelable
class BossBarEvent(val displayData: IBossDisplayData, val hasColorModifier: Boolean) : DebugEvent()

@Cancelable
class PacketReceiveEvent(val packet: Packet<*>): DebugEvent()
@Cancelable
class PacketSentEvent(val packet: Packet<*>) : DebugEvent()


