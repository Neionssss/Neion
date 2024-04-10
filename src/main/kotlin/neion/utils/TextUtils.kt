package neion.utils

import neion.Neion
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

object TextUtils {
    fun info(text: String, prefix: Boolean = true) = Neion.mc.thePlayer.addChatMessage(ChatComponentText("${(if (prefix) "§f§0[Neion]§f§r" else "")}$text§r"))
    fun toggledMessage(message: String, state: Boolean) = info("§r§$2$message §r§$2[${if (state) "§r§$2enabled" else "§r§$3disabled"}§r§$3]§r")
    fun sendCommand(message: String) = sendMessage("/$message")
    fun sendMessage(message: String) = Neion.mc.thePlayer.sendChatMessage(message)
    fun stripControlCodes(string: String?): String = EnumChatFormatting.getTextWithoutFormattingCodes(string ?: "")
    fun CharSequence.matchesAny(vararg sequences: Regex): Boolean = sequences.any { matches(it) }
    fun CharSequence?.containsAny(vararg sequences: CharSequence?) = this != null && sequences.any { it != null && contains(it, true) }
}