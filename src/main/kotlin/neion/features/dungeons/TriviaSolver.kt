package neion.features.dungeons

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import neion.Config
import neion.utils.APIHandler
import neion.utils.Location.inDungeons
import neion.utils.TextUtils.containsAny
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object TriviaSolver {

    var triviaAnswer: String? = null
    var quizData: JsonObject? = null

    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (!Config.quizSolver || !inDungeons) return
        val message = e.message.unformattedText
        if (message.contains("What SkyBlock year is it?")) triviaAnswer = JsonPrimitive("Year ${(floor((System.currentTimeMillis() / 1000.0) - 1560276000) / 446400 + 1)}").asString // Credit Danker's Skyblock Mod
        else {
            if (quizData == null) quizData = APIHandler.getResponse("https://data.skytils.gg/solvers/oruotrivia.json") else
            for (question in quizData!!.entrySet().map { it.key }) if (message.contains(question)) triviaAnswer = quizData!![question].asString
        }
        if (message.containsAny("ⓐ", "ⓑ", "ⓒ") && message.contains(triviaAnswer!!)) e.message = ChatComponentText(e.message.formattedText.replace("§a", "§a§l"))
    }

    @SubscribeEvent
    fun onRenderArmorStandPre(e: RenderLivingEvent.Pre<EntityArmorStand>) {
        if (!Config.quizSolver || !inDungeons || triviaAnswer == null) return
        val name = e.entity
        if (name.customNameTag.contains(triviaAnswer!!)) name.customNameTag = name.customNameTag.replace("§a", "§a§l")
    }
}