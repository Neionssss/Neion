package neion.features

import neion.Config
import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.events.ClickEvent
import neion.events.PacketReceiveEvent
import neion.events.PacketSentEvent
import neion.features.dungeons.*
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.funnymap.map.MapUtils
import neion.ui.GuiRenderer
import neion.utils.Location
import neion.utils.Location.inBoss
import neion.utils.Location.inDungeons
import neion.utils.Location.inSkyblock
import neion.utils.RenderUtil
import neion.utils.TextUtils
import neion.utils.Utils
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.items
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.concurrent.schedule


object RandomStuff {


    private var lastGive = 0L
    var enabled = false

    @SubscribeEvent
    fun onConnect(e: ClientConnectedToServerEvent?) {
        if (Config.autoSB && !inSkyblock) Thread {
            while (mc.thePlayer == null) Thread.sleep(300)
            TextUtils.sendCommand("play sb")
        }.start()
    }


    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (!Config.autoSB || e.type == 2.toByte()) return
        val messagee = e.message.unformattedText
        if (inSkyblock && messagee.startsWith("You were kicked while joining that server!")) {
            TextUtils.info("You have auto SB turned on and will be auto-reconnected in a minute!")
            Timer("ARC", false).schedule(55000) { mc.thePlayer.sendChatMessage("/play sb") }
        } else if (!inSkyblock && listOf("You were kicked while joining that server!", "You tried to rejoin too fast, please try again in a moment!").any { messagee.startsWith(it) }) Timer("ARC", false).schedule(10000) { mc.thePlayer.sendChatMessage("/play sb") }
    }

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (Config.noReverse3rdPerson && mc.gameSettings.thirdPersonView == 2) mc.gameSettings.thirdPersonView = 0
        if (Config.ToggleSprint) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (GuiRenderer.titleTicks > 0) GuiRenderer.titleTicks--
        if (e.phase == TickEvent.Phase.START && mc.currentScreen !is GuiChest && TerminalSolvers.currentTerminal != TerminalSolvers.Terminal.NONE) TerminalSolvers.currentTerminal = TerminalSolvers.Terminal.NONE
        Dungeon.onTick()
        Location.onTick()
        BlazeSolver.onTick()
        if (Config.autoGFS && inDungeons) mc.thePlayer?.inventory?.items?.filter { it?.item == Items.ender_pearl && System.currentTimeMillis() - lastGive > 5000 }?.forEach {
            if (it == null || it.stackSize > Config.minep) return
            TextUtils.sendCommand("gfs ender_pearl ${16 - it.stackSize}")
            lastGive = System.currentTimeMillis()
        }
        if (listFor.isNotEmpty() && enabled) PathFinderer().updateTask(listFor)
    }

    // https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/AntiBlind.kt
    @SubscribeEvent
    fun onRenderFog(event: EntityViewRenderEvent.FogDensity) {
        if (!Config.disableBlind) return
        event.density = 0f
        GlStateManager.setFogStart(998f)
        GlStateManager.setFogEnd(999f)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onMessageSendToServer(e: PacketSentEvent) {
        mapOf(
                "//ai" to "/p settings allinvite",
                "/d" to "warp dhub",
                "/pko" to "p kickoffline",
                "/f1" to "joininstance CATACOMBS_FLOOR_ONE",
                "/f2" to "joininstance CATACOMBS_FLOOR_TWO",
                "/f3" to "joininstance CATACOMBS_FLOOR_THREE",
                "/f4" to "joininstance CATACOMBS_FLOOR_FOUR",
                "/f5" to "joininstance CATACOMBS_FLOOR_FIVE",
                "/f6" to "joininstance CATACOMBS_FLOOR_SIX",
                "/f7" to "joininstance CATACOMBS_FLOOR_SEVEN",
                "/m1" to "joininstance MASTER_CATACOMBS_FLOOR_ONE",
                "/m2" to "joininstance MASTER_CATACOMBS_FLOOR_TWO",
                "/m3" to "joininstance MASTER_CATACOMBS_FLOOR_THREE",
                "/m4" to "joininstance MASTER_CATACOMBS_FLOOR_FOUR",
                "/m5" to "joininstance MASTER_CATACOMBS_FLOOR_FIVE",
                "/m6" to "joininstance MASTER_CATACOMBS_FLOOR_SIX",
                "/m7" to "joininstance MASTER_CATACOMBS_FLOOR_SEVEN"
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
        if (Config.pets.isActive) TextUtils.sendCommand("pets")
        if (Config.trades.isActive) TextUtils.sendCommand("trades")
        if (Config.equipment.isActive) TextUtils.sendCommand("equipment")
        if (Config.wardrobe.isActive) TextUtils.sendCommand("wardrobe")
        if (Config.autorun.isActive) KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, true)
        if (Config.autoSellBind.isActive) {
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
        if (Config.freeBind.isActive) Config.freeCam = !Config.freeCam
    }

    var listFor = mutableListOf<Vec3>()

    @SubscribeEvent
    fun onLeftClick(e: ClickEvent.LeftClickEvent) {
        if (mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val realCoords = mc.objectMouseOver.blockPos
        val pos = Vec3(realCoords.x.toDouble(), realCoords.y.toDouble(), realCoords.z.toDouble())

        if (pos !in listFor) listFor.add(pos)
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        listFor.forEachIndexed { i, blaze ->
            val pos = listFor[(listFor.indexOf(blaze) + 1).coerceAtMost(listFor.lastIndex)]
            RenderUtil.draw3DLine(
                blaze, pos, 4f, Color.blue
            )
        }
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(e: RenderBlockOverlayEvent) {
        e.isCanceled = Config.randomStuff && inSkyblock
    }

    @SubscribeEvent
    fun onLivingDeath(e: LivingDeathEvent) {
        if (Config.hideDeathAnimation && inSkyblock) e.entity.setDead()
        RunInformation.onEntityDeath(e)
    }

    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        RunInformation.onScoreboard(e)
        val packet = e.packet as? S2DPacketOpenWindow ?: return
        if (Config.autoclose && inDungeons && packet.windowTitle.unformattedText.equalsOneOf("Chest", "Large Chest")) {
            e.isCanceled = true
            mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
        }
    }

    @SubscribeEvent
    fun onWorld(e: WorldEvent.Unload) {
        MurderHelper.murder.clear()
        MurderHelper.bowHolder.clear()
        DungeonChestProfit.canOpen = false
        SimonSaysSolver.clickInOrder.clear()
        SimonSaysSolver.cleared = false
        TeleportMazeSolver.map.clear()
        TeleportMazeSolver.rightOne = null
        WeirdosSolver.riddleChest = null
        WeirdosSolver.notYet = true
        inDungeons = false
        Location.dungeonFloor = -1
        inBoss = false
        DungeonChestProfit.DungeonChest.entries.forEach(DungeonChestProfit.DungeonChest::reset)
        DungeonChestProfit.noobmen.clear()
        TriviaSolver.triviaAnswer = null
        MapUtils.saveExtras()
        Dungeon.reset()
        Config.freeCam = false
    }
}



