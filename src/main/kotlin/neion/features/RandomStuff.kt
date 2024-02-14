package neion.features

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils
import neion.Config
import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.events.PacketReceiveEvent
import neion.events.PacketSentEvent
import neion.features.dungeons.BlazeSolver
import neion.features.dungeons.DungeonChestProfit
import neion.features.dungeons.TriviaSolver
import neion.features.dungeons.terminals.SimonSaysSolver
import neion.funnymap.Dungeon
import neion.funnymap.map.ScanUtils
import neion.ui.GuiRenderer
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.matchesAny
import neion.utils.Utils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import org.lwjgl.opengl.Display
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.exp
import kotlin.random.Random


object RandomStuff {


    @SubscribeEvent
    fun onConnect(e: ClientConnectedToServerEvent?) {
        if (Config.autoSB && !inSkyblock) {
            Thread {
                while (mc.thePlayer == null) Thread.sleep(300)
                TextUtils.sendCommand("play sb")
            }.start()
        }
    }


    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (!Config.autoSB || !e.message.unformattedText.contains("You were kicked while joining that server!") || !inSkyblock) return
        TextUtils.info("You have auto SB turned on and will be auto-reconnected in a minute!")
        Timer("ARC", false).schedule(50000) { mc.thePlayer.sendChatMessage("/play sb") }
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (Config.noReverse3rdPerson && mc.gameSettings.thirdPersonView == 2) mc.gameSettings.thirdPersonView = 0
        if (Config.ToggleSprint) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
        if (Config.fakeHaste && inDungeons) mc.thePlayer.addPotionEffect(PotionEffect(3, 999999999, 2))
        if (Config.funnyItems) mc.thePlayer.swingProgress = (length() * exp(-Random.nextFloat()))
        mc.mcProfiler.startSection("fmspsa")
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (GuiRenderer.titleTicks > 0) GuiRenderer.titleTicks--
        Dungeon.onTick()
        Location.onTick()
        BlazeSolver.onTick()
        mc.mcProfiler.endSection()
        Display.setTitle(Config.mcTitle)
    }

    fun length(): Int =
        if (Config.fakeHaste) 6 else if (mc.thePlayer.isPotionActive(Potion.digSpeed)) 6 - (1 + mc.thePlayer.getActivePotionEffect(Potion.digSpeed).amplifier) else (if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) 6 + (1 + mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).amplifier) * 2 else 6)

    // https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/AntiBlind.kt
    @SubscribeEvent
    fun onRenderFog(event: EntityViewRenderEvent.FogDensity) {
        if (!Config.disableBlind || !inSkyblock) return
        event.density = 0f
        GlStateManager.setFogStart(998f)
        GlStateManager.setFogEnd(999f)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onMessageSendToServer(e: PacketSentEvent) {
        mapOf(
            "/d" to "warp dhub",
            "/pko" to "p kickoffline",
            "/f1" to "joininstance CATACOMBS_FLOOR_ONE",
            "/f2" to "joininstance CATACOMBS_FLOOR_TWO",
            "/f3" to "joininstance CATACOMBS_FLOOR_THREE",
            "/f4" to "joininstance CATACOMBS_FLOOR_FOUR",
            "/f5" to "joininstance CATACOMBS_FLOOR_FIVE",
            "/f6" to "joininstance CATACOMBS_FLOOR_SIX",
            "/f7" to "joininstance CATACOMBS_FLOOR_SEVEN"
        ).forEach {
            if ((e.packet as? C01PacketChatMessage)?.message?.lowercase() == it.key) {
                e.isCanceled = true
                TextUtils.sendCommand(it.value)
            }
        }
    }

    @SubscribeEvent
    fun onKey(e: InputEvent.KeyInputEvent) {
        if (Config.fuck.isActive) Config.openGui()
        if (Config.trades.isActive) TextUtils.sendCommand("trades")
        if (Config.equipment.isActive) TextUtils.sendCommand("equipment")
        if (Config.pets.isActive) TextUtils.sendCommand("pets")
        if (Config.wardrobe.isActive) TextUtils.sendCommand("wardrobe")
        if (Config.autorun.isActive) KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, true)
        if (Config.freeBind.isActive) Config.freeCam = !Config.freeCam
        if (Config.autoSellBind.isActive) onSell()
    }


    @SubscribeEvent
    fun onRenderBlockOverlay(e: RenderBlockOverlayEvent) {
        if (Config.randomStuff && inSkyblock) e.isCanceled = true
    }

    @SubscribeEvent
    fun onLivingDeath(e: LivingDeathEvent) {
        if (Config.hideDeathAnimation && inSkyblock) e.entity.setDead()
    }

    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        val packet = e.packet
        val particle = packet as? S2APacketParticles
        if (particle?.particleType?.particleID == 25 && Config.HideEnchantRune || particle?.particleType?.particleID == 34 && Config.hideHeartParticles) e.isCanceled = true
        if (Config.autoclose && inDungeons) {
            if ((packet as? S2DPacketOpenWindow ?: return).windowTitle.unformattedText.equalsOneOf("Chest", "Large Chest")) {
                e.isCanceled = true
                mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
            }
        }
    }

    // Scuffed as fuck
    fun onSell() {
        TextUtils.sendCommand("bz")
        Timer("wwww").schedule(350) {

            if ((mc.thePlayer?.openContainer as? ContainerChest)?.lowerChestInventory?.displayName?.unformattedText?.contains(
                    "bazaar",
                    true
                )!!
            ) Utils.clickSlot(47)
            Timer("ssss").schedule(400) {
                if ((mc.thePlayer.openContainer as? ContainerChest)?.lowerChestInventory?.displayName?.unformattedText?.contains(
                        "are you sure",
                        true
                    )!!
                ) Utils.clickSlot(11)
            }
        }
    }

    @SubscribeEvent
    fun onWorld(e: WorldEvent.Unload) {
        MurderHelper.murder.clear()
        MurderHelper.bowHolder.clear()
        BlazeSolver.blist.clear()
        MurderHelper.wrote = false
        JasperESP.espModeMap.clear()
        DungeonChestProfit.canOpen = false
        DungeonChestProfit.noobmen.clear()
        SimonSaysSolver.clickInOrder.clear()
        SimonSaysSolver.cleared = false
        inDungeons = false
        Location.dungeonFloor = -1
        Location.inBoss = false
        DungeonChestProfit.DungeonChest.entries.forEach(DungeonChestProfit.DungeonChest::reset)
        TriviaSolver.triviaAnswer = null
        ScanUtils.saveExtras()
        Dungeon.reset()
        if (Config.freeCam) Config.freeCam = false
    }

    var tabFooterAdvertisement: String = "Ranks, Boosters & MORE! STORE.HYPIXEL.NET"
    var tabHeaderAdvertisement: String = "You are playing on MC.HYPIXEL.NET"

    fun modifyHeader(instance: FontRenderer, formattedHeader: String, wrapWidth: Int): List<String> {
        if (Config.cleanerTab && HypixelUtils.INSTANCE.isHypixel && formattedHeader.trim().containsAny(tabHeaderAdvertisement, tabFooterAdvertisement)) {
            if (formattedHeader.trim().matchesAny(tabHeaderAdvertisement.toRegex(), tabFooterAdvertisement.toRegex())) return ArrayList()
        }
        return instance.listFormattedStringToWidth(formattedHeader, wrapWidth - 50)
    }
}