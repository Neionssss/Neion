package neion.features

import neion.mixins.AccessorGuiNewChat
import neion.ui.clickgui.Module
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.client.gui.*
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import java.lang.reflect.Field

object CopyChat: Module("Chat Copy") {

    var hoveredChatLine: ChatLine? = null

    @SubscribeEvent
    fun preDrawScreen(e: GuiScreenEvent.DrawScreenEvent.Pre) {
        val chat = mc.ingameGUI.chatGUI
        hoveredChatLine = if (chat.chatOpen) chat.getChatLine(Mouse.getX(), Mouse.getY()) else null
    }

    @SubscribeEvent
    fun onAttemptCopy(e: GuiScreenEvent.MouseInputEvent.Pre) {
        if (e.gui !is GuiChat || !Mouse.getEventButtonState() || Mouse.getEventButton() != 1 || !mc.ingameGUI.chatGUI.chatOpen) return
        val component = hoveredChatLine?.chatComponent?.unformattedText?.stripControlCodes() ?: return
        GuiScreen.setClipboardString(component)
        mc.thePlayer.addChatMessage(ChatComponentText("Copied Chat Message").apply { chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(component))) })
    }

    fun GuiNewChat.getChatLine(mouseX: Int, mouseY: Int): ChatLine? {
        if (this is AccessorGuiNewChat) {
            val scaleFactor = ScaledResolution(mc).scaleFactor
            val extraOffset = if (getClassHelper("club.sk1er.patcher.config.PatcherConfig")?.getFieldHelper("chatPosition")?.getBoolean(null) == true) 12 else 0
            val x = MathHelper.floor_float((mouseX / scaleFactor - 3).toFloat() / chatScale)
            val y = MathHelper.floor_float((mouseY / scaleFactor - 27 - extraOffset).toFloat() / chatScale)

            if (x and y >= 0) {
                val l = lineCount.coerceAtMost(drawnChatLines.size)
                if (x <= MathHelper.floor_float(chatWidth.toFloat() / chatScale) && y < mc.fontRendererObj.FONT_HEIGHT * l + l) {
                    val lineNum = y / mc.fontRendererObj.FONT_HEIGHT + scrollPos
                    if (lineNum >= 0 && lineNum < drawnChatLines.size) return drawnChatLines[lineNum]
                }
            }
        }
        return null
    }

    val classes = hashMapOf<String, Class<*>>()
    val fields2 = hashMapOf<String, Field>()

    fun Class<*>.getFieldHelper(fieldName: String) = runCatching { fields2.getOrPut("$name $fieldName") { getDeclaredField(fieldName).apply { isAccessible = true } } }.getOrNull()
    fun getClassHelper(className: String) = runCatching { classes.getOrPut(className) { Class.forName(className) } }.getOrNull()

}
