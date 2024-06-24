package neion.features.dungeons

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import neion.events.ChatEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.APIHandler
import neion.utils.Location.inDungeons
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.floor

object TriviaSolver: Module("Quiz Solver", category = Category.DUNGEON) {

    var triviaAnswer: JsonArray? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(e: ClientChatReceivedEvent) {
        if (!inDungeons) return
        val message = e.message.unformattedText.stripControlCodes()
        if (message.contains("What SkyBlock year is it?")) {
            triviaAnswer = JsonArray()
            triviaAnswer?.add(JsonPrimitive("Year ${(floor((System.currentTimeMillis() / 1000L).toDouble() - 1560276000) / 446400 + 1).toInt()}"))
        } // Credit Danker's Skyblock Mod
        else {
            if (APIHandler.quizdata == null) APIHandler.quizdata =
                APIHandler.getResponse("https://data.skytils.gg/solvers/oruotrivia.json")
            val triviaSolutions = APIHandler.quizdata!!.asJsonObject
            for (question in triviaSolutions.entrySet().map { it.key }) {
                if (message.contains(question)) {
                    triviaAnswer = triviaSolutions.getAsJsonArray(question)
                }
            }
        }
        if (message.containsAny("ⓐ", "ⓑ", "ⓒ") && triviaAnswer != null) {
            for (answer in triviaAnswer!!) {
                // https://i.imgur.com/zpwaSOq.png
                e.message = if (message.contains(answer.asString)) ChatComponentText(
                    e.message.formattedText.replace(
                        "§a",
                        "§a§l"
                    )
                ) else ChatComponentText(e.message.formattedText.replace("§a", "§a§4"))
            }
        }
    }

    @SubscribeEvent
    fun onRenderArmorStandPre(e: RenderLivingEvent.Pre<EntityArmorStand>) {
        if (!inDungeons || triviaAnswer == null) return
        for (answer in triviaAnswer!!) {
            val name = e.entity
            if (name.customNameTag.contains(answer.asString)) name.customNameTag = "CLICK!!!"
        }
    }
}