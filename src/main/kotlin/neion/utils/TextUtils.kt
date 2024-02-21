package neion.utils

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent
import neion.Neion
import net.minecraft.util.ChatComponentText
import kotlin.random.Random

object TextUtils {
    fun info(text: String, prefix: Boolean = true) {
        if (Neion.mc.thePlayer == null) return

        val textPrefix = if (prefix) "${Neion.CHAT_PREFIX} " else ""
        Neion.mc.thePlayer.addChatMessage(ChatComponentText("$textPrefix$text§r"))
    }

    fun toggledMessage(message: String, state: Boolean) {
        val rInt = Random.nextInt(9)
        val stateText = if (state) "§r§${rInt}enabled" else "§r§${rInt}disabled"
        info("§r§$rInt$message §r§$rInt[$stateText§r§$rInt]§r")
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
    fun CharSequence.matchesAny(sequences: List<Regex>): Boolean = sequences.any { matches(it) }
    fun CharSequence?.containsAny(vararg sequences: CharSequence?): Boolean {
        if (this == null) return false
        return sequences.any { it != null && this.contains(it, true) }
    }
    fun CharSequence?.startsWithAny(vararg sequences: CharSequence?): Boolean {
        if (this == null) return false
        return sequences.any { it != null && this.startsWith(it, true) }
    }
}