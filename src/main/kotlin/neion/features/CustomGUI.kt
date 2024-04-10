package neion.features

import neion.Config
import neion.events.ChatEvent
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.equalsOneOf
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CustomGUI {


    var secretss = 0
    var maxSecretss = 0
    var leastMana = 0
    var maxedMana = 0
    var leastHealth = 0
    var maxHealth = 0
    private val secretRegex = Regex("(?<secrets>\\d+)/(?<maxSecrets>\\d+) Secrets")
    private val manaRegex = Regex("(?<lmana>\\d+)/(?<mmana>\\d+). Mana")
    private val healthRegex = Regex("(?<lHealth>\\d+)/(?<mHealth>\\d+)❤")

    @SubscribeEvent
    fun onSPacket(e: ClientChatReceivedEvent) {
        if (Config.onlySkyblock && !inSkyblock) return
        if (Config.cancelServerMessages && e.type != 2.toByte() && !e.message.unformattedText.containsAny("ⓐ", "ⓑ", "ⓒ")) e.isCanceled = true
        if (e.type == 2.toByte()) {
            if (Config.hideActionbar) e.isCanceled = true
            if (Config.showSecretsFocus) e.message = ChatComponentText(e.message.unformattedText.replace(secretRegex, ""))
            if (Config.showManaFocus) e.message = ChatComponentText(e.message.unformattedText.replace(",","").replace(manaRegex, ""))
            if (Config.showHealth) e.message = ChatComponentText(e.message.unformattedText.replace(",","").replace(healthRegex, ""))
        }
    }

    @SubscribeEvent
    fun onChat(e: ChatEvent) {
        if (e.packet.type == 2.toByte()) {
            secretRegex.find(e.text)?.destructured?.let { (secrets, maxSecrets) ->
                secretss = secrets.toInt()
                maxSecretss = maxSecrets.toInt()
            }
            manaRegex.find(e.text.replace(",", ""))?.destructured?.let { (lmana, mmana) ->
                leastMana = lmana.toInt()
                maxedMana = mmana.toInt()
            }
            healthRegex.find(e.text.replace(",",""))?.destructured?.let { (lHealth,mHealth) ->
                leastHealth = lHealth.toInt()
                maxHealth = mHealth.toInt()
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlayPre(e: RenderGameOverlayEvent) {
        if (Config.onlySkyblock && !inSkyblock) return
        if (Config.hideGUI && e.type.equalsOneOf(
                RenderGameOverlayEvent.ElementType.FOOD,
                RenderGameOverlayEvent.ElementType.AIR,
                RenderGameOverlayEvent.ElementType.ARMOR,
                RenderGameOverlayEvent.ElementType.HEALTHMOUNT,
                RenderGameOverlayEvent.ElementType.PORTAL)) e.isCanceled = true
        if (Config.hideExperience && e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) e.isCanceled = true
        if (Config.hideHealth && e.type == RenderGameOverlayEvent.ElementType.HEALTH) e.isCanceled = true
    }


    fun shouldNot(displayData: IBossDisplayData): Boolean {
        if (Config.onlySkyblock && !inSkyblock) return false
        if (Config.BossBarHider && displayData.displayName.unformattedText.containsAny(
                "Bonzo",
                "Scarf",
                "The Professor",
                "Thorn",
                "Livid",
                "Sadan",
                "Maxor",
                "Storm",
                "Goldor",
                "Necron",
                "The Watcher",
                "Kuudra")) return false
        return true
    }
}