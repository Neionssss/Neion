package neion.features.dungeons

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import neion.Config
import neion.Neion.Companion.mc
import neion.utils.APIHandler
import neion.utils.Location.inDungeons
import neion.utils.TextUtils.containsAny
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object TriviaSolver {

    var triviaAnswer: String? = null
    var triviaAnswersJson = JsonArray()


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(e: ClientChatReceivedEvent) {
        if (!Config.quizSolver || !inDungeons) return
        val message = e.message.unformattedText
        if (message.contains("What SkyBlock year is it?")) {
            triviaAnswersJson = JsonArray()
            triviaAnswersJson.add(JsonPrimitive("Year ${(floor((System.currentTimeMillis() / 1000L).toDouble() - 1560276000) / 446400 + 1).toInt()}")) // Credit Danker's Skyblock Mod
        } else {
            if (APIHandler.quizdata == null) APIHandler.quizdata = APIHandler.getResponse("https://data.skytils.gg/solvers/oruotrivia.json")
            val triviaSolutions = APIHandler.quizdata!!.asJsonObject
            for (question in triviaSolutions.entrySet().map { it.key }) {
                if (message.contains(question)) triviaAnswersJson = triviaSolutions.getAsJsonArray(question)
            }
        }

        if (message.containsAny("ⓐ", "ⓑ", "ⓒ")) {
            // https://i.imgur.com/zpwaSOq.png
            for (i in 0 until triviaAnswersJson.size()) {
                val solution = triviaAnswersJson[i].asString
                if (message.contains(solution)) {
                    triviaAnswer = solution
                    e.message = if (mc.languageManager.currentLanguage.languageCode == "ru_RU") ChatComponentText("     " + EnumChatFormatting.RED + message[7] + " Это победа") else ChatComponentText("     " + EnumChatFormatting.RED + message[7] + " Right Answer")
                } else e.message = ChatComponentText("     " + EnumChatFormatting.DARK_GREEN + message[7] + " Wrong Answer")
            }
        }
    }

    @SubscribeEvent
    fun onRenderArmorStandPre(e: RenderLivingEvent.Pre<EntityArmorStand>) {
        if (!Config.quizSolver || !inDungeons || triviaAnswer == null) return
        val name = e.entity
            if (name.customNameTag.contains(triviaAnswer!!)) {
                if (mc.languageManager.currentLanguage.languageCode == "ru_RU") name.customNameTag = "ЖМИ!!!" else name.customNameTag = "Why don't you click me?"
            }
        }
    }


