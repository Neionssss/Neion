package neion.utils

import neion.Neion
import neion.Neion.Companion.mc
import net.minecraft.util.ChatComponentText
import net.minecraft.util.StringUtils
import kotlin.random.Random

object TextUtils {

    fun info(text: String, prefix: Boolean = true) {
        val textPrefix = if (prefix) "${Neion.CHAT_PREFIX} " else ""
        mc.thePlayer?.addChatMessage(ChatComponentText("$textPrefix$text§r"))
    }

    fun toggledMessage(message: String, state: Boolean) {
        val rInt = Random.nextInt(9)
        val stateText = if (state) "§r§1enabled" else "§r§4disabled"
        info("§r§$rInt$message §r§$rInt[$stateText§r§$rInt]§r")
    }

    fun sendCommand(message: String) = sendMessage("/$message")
    fun sendMessage(message: String) = mc.thePlayer.sendChatMessage(message)
    fun String?.stripControlCodes(): String = StringUtils.stripControlCodes(this ?: "")
    fun CharSequence.containsAny(sequences: Iterable<CharSequence>): Boolean = sequences.any { contains(it) }
    fun CharSequence.matchesAny(vararg sequences: Regex): Boolean = sequences.any { matches(it) }
    fun CharSequence.matchesAny(sequences: List<Regex>): Boolean = sequences.any { matches(it) }
    fun CharSequence?.containsAny(vararg sequences: CharSequence?) =  this != null && sequences.any { it != null && contains(it, true) }
    fun CharSequence?.startsWithAny(vararg sequences: CharSequence?) = this != null && sequences.any { it != null && startsWith(it, true) }

    private var fontRenderer = mc.fontRendererObj

    fun getStringWidth(text: String?) = fontRenderer.getStringWidth(StringUtils.stripControlCodes(text))

    fun getSplitHeight(text: String, wrapWidth: Int): Int {
        var dy = 0
        for (s in fontRenderer.listFormattedStringToWidth(text, wrapWidth)) dy += fontRenderer.FONT_HEIGHT
        return dy
    }

    val fontHeight: Int
        get() = fontRenderer.FONT_HEIGHT

    fun drawString(text: String?, x: Int, y: Int, color: Int, shadow: Boolean = false) {
        fontRenderer.drawString(text, x.toFloat(), y.toFloat(), color, shadow)
    }

    fun drawCenteredString(text: String?, x: Int, y: Int, color: Int) {
        drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color)
    }

    fun drawCenteredStringWithShadow(text: String?, x: Int, y: Int, color: Int) {
        drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color, true)
    }

    fun drawTotalCenteredStringWithShadow(text: String?, x: Int, y: Int, color: Int) {
        drawString(
            text,
            x - fontRenderer.getStringWidth(text) / 2,
            y - fontRenderer.FONT_HEIGHT / 2,
            color,
            true
        )
    }

    /**
     * Draws a string with line wrapping.
     */
    fun drawSplitString(text: String, x: Int, y: Int, wrapWidth: Int, color: Int) {
        fontRenderer?.drawSplitString(text, x, y, wrapWidth, color)
    }

    fun String.capitalizeOnlyFirst() = substring(0, 1).uppercase() + substring(1, length).lowercase()
}