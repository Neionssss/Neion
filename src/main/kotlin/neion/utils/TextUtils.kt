package neion.utils

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent
import neion.Neion
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraft.util.StringUtils

object TextUtils {
    fun info(text: String, prefix: Boolean = true) {
        if (Neion.mc.thePlayer == null) return

        val textPrefix = if (prefix) "${Neion.CHAT_PREFIX} " else ""
        Neion.mc.thePlayer.addChatMessage(ChatComponentText("$textPrefix$text§r"))
    }

    fun toggledMessage(message: String, state: Boolean) {
        val stateText = if (state) "§aenabled" else "§cdisabled"
        info("§9$message §8[$stateText§8]§r")
    }

    fun sendPartyChatMessage(message: String) {
        sendMessage("/pc $message")
    }

    fun sendCommand(message: String) {
        sendMessage("/$message")
    }

    fun sendMessage(message: String) {
        Neion.mc.thePlayer.sendChatMessage(message)
    }

    fun String?.stripControlCodes(): String = UTextComponent.stripFormatting(this ?: "")

    fun CharSequence.containsAny(sequences: Iterable<CharSequence>): Boolean = sequences.any { contains(it) }
    fun CharSequence.matchesAny(vararg sequences: Regex): Boolean = sequences.any { matches(it) }
    fun CharSequence?.containsAny(vararg sequences: CharSequence?): Boolean {
        if (this == null) return false
        return sequences.any { it != null && this.contains(it, true) }
    }
}