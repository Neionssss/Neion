package neion.events

import neion.utils.TextUtils.stripControlCodes
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

class ChatEvent(val packet: S02PacketChat) : DebugEvent() {
    val text: String by lazy { packet.chatComponent.unformattedText.stripControlCodes() }
}

@Cancelable
class PacketReceiveEvent(val packet: Packet<*>): DebugEvent()
@Cancelable
class PacketSentEvent(val packet: Packet<*>) : DebugEvent()

class PreKeyInputEvent(val key: Int, val character: Char) : DebugEvent()

class PreMouseInputEvent(val button: Int): DebugEvent()


