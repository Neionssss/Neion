package neion.features

import neion.events.ChatEvent
import neion.features.dungeons.DungeonChestProfit.profitScale
import neion.funnymap.RunInformation
import neion.ui.HudElement
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.ColorSetting
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.Location
import neion.utils.Location.inSkyblock
import neion.utils.RenderUtil
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object CustomGUI: Module("CustomGUI", category = Category.RENDER) {

    val secretX = NumberSetting("SecretX", default = 500.0, hidden = true)
    val secretY = NumberSetting("SecretY", default = 470.0, hidden = true)
    val secretScale = NumberSetting("SecretScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)

    val clearedX = NumberSetting("ClearedX", default = 840.0, hidden = true)
    val clearedY = NumberSetting("ClearedY", default = 470.0, hidden = true)
    val clearedScale = NumberSetting("ClearedScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)

    val timeX = NumberSetting("TimeX", default = 840.0, hidden = true)
    val timeY = NumberSetting("TimeY", default = 200.0, hidden = true)
    val timeScale = NumberSetting("TimeScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)

    val manaX = NumberSetting("ManaX", default = 500.0, hidden = true)
    val manaY = NumberSetting("ManaY", default = 500.0, hidden = true)
    val manaScale = NumberSetting("ManaScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)

    val healthX = NumberSetting("HealthX", default = 800.0, hidden = true)
    val healthY = NumberSetting("HealthY", default = 800.0, hidden = true)
    val healthScale = NumberSetting("HealthScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)

// -----------------------------------------------------------------------------------
    val onlySkyblock = BooleanSetting("Only on Skyblock", enabled = true)
    val cancelMessages = BooleanSetting("Hide Messages", description = "You will not see any incoming messages, but your client will still be able to process them. T.E it doesn't ruin any chat-assisted solvers")
    val hideHand = BooleanSetting("Hide Hand")
    val hideGUI = BooleanSetting("Hide useless GUI", description = "Hides food,armor,horse armor huds, since they're not useful on SkyBlock")
    val hideScoreboard = BooleanSetting("Hide Scoreboard")
    val hideBossBar = BooleanSetting("Hide Boss Bar", description = "Doesn't hide Maxor, Storm etc..")
    val hideExperience = BooleanSetting("Hide Experience bar")
    val hideHealth = BooleanSetting("Hide Health Bar")
    val hideActionBar = BooleanSetting("Hide Action Bar")
    val showMana = BooleanSetting("Custom Mana")
    val customHealth = BooleanSetting("Custom Health")
    val showSecrets = BooleanSetting("Dungeon Secrets")
    val customClearedPercent = BooleanSetting("Cleared Percent")
    val customTime = BooleanSetting("Dungeon Time Spent")
    val manaColor = ColorSetting("Mana Color", default = Color.blue)
    val healthColor = ColorSetting("Health Color", default = Color.red)
    val clearedPercentColor = ColorSetting("Cleared Percent Color", default = Color.cyan)
    val timeColor = ColorSetting("Time Color", default = Color.yellow)

    init {
        addSettings(
            hideHand,
            hideScoreboard,
            secretX,
            secretY,
            clearedX,
            clearedY,
            timeX,
            timeY,
            manaX,
            manaY,
            healthX,
            healthY,
            secretScale,
            clearedScale,
            timeScale,
            manaScale,
            healthScale,
            onlySkyblock,
            cancelMessages,
            hideGUI,
            hideBossBar,
            hideExperience,
            hideHealth,
            hideActionBar,
            showMana,
            customHealth,
            showSecrets,
            customClearedPercent,
            customTime
        )
    }

    var secrets = 0
    var maxSecrets = 0
    var leastMana = 0
    var maxedMana = 0
    var leastHealth = 0
    var maxedHealth = 0
    val secretRegex = Regex("\\s*§7(?<secrets>\\d+)/(?<maxSecrets>\\d+) Secrets")
    val manaRegex = Regex("(?<lmana>\\d+)/(?<mmana>\\d+)✎ Mana")
    val healthRegex = Regex("(?<lh>\\d+)/(?<mh>\\d+)❤")

    @SubscribeEvent
    fun onSPacket(e: ClientChatReceivedEvent) {
        if (onlySkyblock.enabled && !inSkyblock) return
        if (cancelMessages.enabled && e.type != 2.toByte() && !e.message.unformattedText.containsAny("ⓐ", "ⓑ", "ⓒ")) e.isCanceled = true
        if (e.type == 2.toByte()) {
            if (hideActionBar.enabled) e.isCanceled = true
            if (showSecrets.enabled) e.message = ChatComponentText(e.message.unformattedText.replace(secretRegex, ""))
            if (showMana.enabled) e.message = ChatComponentText(e.message.unformattedText.replace(",","").replace(manaRegex, ""))
            if (customHealth.enabled) e.message = ChatComponentText(e.message.unformattedText.replace(",", "").replace(healthRegex,""))
        }
    }

    @SubscribeEvent
    fun onChat(e: ChatEvent) {
        if (e.packet.type == 2.toByte()) {
            secretRegex.find(e.packet.chatComponent.formattedText)?.destructured?.let { (secretss, maxSecretss) ->
                secrets = secretss.toInt()
                maxSecrets = maxSecretss.toInt()
            }
            manaRegex.find(e.text.replace(",", ""))?.destructured?.let { (lmana, mmana) ->
                leastMana = lmana.toInt()
                maxedMana = mmana.toInt()
            }
            healthRegex.find(e.text.replace(",", ""))?.destructured?.let { (lh,mh) ->
                leastHealth = lh.toInt()
                maxedHealth = mh.toInt()
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlayPre(e: RenderGameOverlayEvent) {
        if (!enabled || (onlySkyblock.enabled && !inSkyblock)) return
        if (hideGUI.enabled && e.type.equalsOneOf(
                RenderGameOverlayEvent.ElementType.FOOD,
                RenderGameOverlayEvent.ElementType.AIR,
                RenderGameOverlayEvent.ElementType.ARMOR,
                RenderGameOverlayEvent.ElementType.HEALTHMOUNT,
                RenderGameOverlayEvent.ElementType.PORTAL)) e.isCanceled = true
        if ((hideExperience.enabled && e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) || (hideHealth.enabled && e.type == RenderGameOverlayEvent.ElementType.HEALTH)) e.isCanceled = true
    }

    fun shouldHideBossBar(displayData: IBossDisplayData): Boolean {
        if (onlySkyblock.enabled && !inSkyblock) return false
            return enabled && !displayData.displayName?.unformattedText?.containsAny( "Bonzo",
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
            "Kuudra")!!
    }


    object DungeonSecretDisplay : HudElement(secretX, secretY, width = mc.fontRendererObj.getStringWidth("$secrets/$maxSecrets Secrets") + 1, scaleSett = secretScale) {
        val color: Color = when (secrets / maxSecrets.toDouble()) {
            in 0.0..0.5 -> Color.red
            in 0.5..0.75 -> Color.yellow
            else -> Color.green
        }
        override fun shouldRender() = Location.inDungeons && RunInformation.started && CustomGUI.enabled && showSecrets.enabled
        override fun render() = RenderUtil.renderText("Secrets: $secrets/$maxSecrets", 0, 0, color = color.rgb)
    }
    object ClearedDisplay : HudElement(clearedX, clearedY, width = mc.fontRendererObj.getStringWidth("Cleared 100%"), scaleSett = clearedScale) {
        override fun shouldRender() = Location.inDungeons && RunInformation.started && CustomGUI.enabled && customClearedPercent.enabled
        override fun render() = RenderUtil.renderText(Location.getLines().find { Location.cleanLine(it).contains("Cleared: ") }?.substringBefore("(") ?: "Cleared 100%", 0, 0, color = clearedPercentColor.rgb)
    }
    object TimeDisplay : HudElement(timeX,timeY,width = mc.fontRendererObj.getStringWidth("Time Elapsed: 2m 40s") + 1, scaleSett = timeScale) {
        override fun shouldRender() = Location.inDungeons && RunInformation.started && CustomGUI.enabled && customTime.enabled
        override fun render() = RenderUtil.renderText(Location.getLines().find { Location.cleanLine(it).contains("Time Elapsed:") } ?: "Time Elapsed: 2m 40s", 0, 0, color = timeColor.rgb)
    }
    object ManaDisplay : HudElement(manaX,manaY,width = mc.fontRendererObj.getStringWidth("10000/10000 Mana"),scaleSett = manaScale) {
        override fun shouldRender() = CustomGUI.enabled && showMana.enabled && inSkyblock
        override fun render() = RenderUtil.renderText("Mana: $leastMana/$maxedMana", 0, 0, color = manaColor.rgb)
    }
    object HealthDisplay : HudElement(healthX, healthY, width = mc.fontRendererObj.getStringWidth("10000/10000 Health"), scaleSett = healthScale) {
        override fun shouldRender() = CustomGUI.enabled && customHealth.enabled && inSkyblock
        override fun render() = RenderUtil.renderText("Health: $leastHealth/$maxedHealth", 0, 0, color = healthColor.rgb)
    }

}