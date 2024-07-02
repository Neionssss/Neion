package neion.features

import io.netty.util.internal.ConcurrentSet
import neion.Neion
import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.events.PacketReceiveEvent
import neion.events.PacketSentEvent
import neion.features.dungeons.*
import neion.features.dungeons.ChestProfit.DungeonChest
import neion.funnymap.Dungeon
import neion.ui.EditHudGUI
import neion.ui.EditHudGUI.hudElements
import neion.utils.*
import neion.utils.ExtrasConfig.extraRooms
import neion.utils.ExtrasConfig.gson
import neion.utils.Location.inBoss
import neion.utils.Location.inDungeons
import neion.utils.Location.inSkyblock
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

object RandomStuff {

    var lastLength = 0L
    var shouldReset = false
    var map = ConcurrentSet<BlockPos>()

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (e.phase == TickEvent.Phase.START && mc.currentScreen !is GuiChest && TerminalSolvers.currentTerminal != TerminalSolvers.Terminal.NONE) TerminalSolvers.currentTerminal =
            TerminalSolvers.Terminal.NONE
        Location.onTick()
        Dungeon.onTick()
        val hue = ClickGui.color.hue
        val increment =
            if (hue >= 0f && hue < 1f) ClickGui.chromaSpeed.value / 1000 else if (hue == 1f) -ClickGui.chromaSpeed.value / 1000 else 0.0
        if (ClickGui.chroma.enabled) ClickGui.color.hue += increment.toFloat()
        if (inDungeons && MapUtils.getCurrentRoom()?.data?.name == "Ice Fill") {
            val blocks = BlockPos.getAllInBox(
                BlockPos(mc.thePlayer.posX - 10, mc.thePlayer.posY - 1, mc.thePlayer.posZ - 10),
                BlockPos(mc.thePlayer.posX + 10, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 10)
            )
            map.addAll(blocks.filter { mc.theWorld.getBlockState(it).block == Blocks.packed_ice })
            map.forEach {
                val blockPos = BlockPos(it.x, it.y + 1, it.z)
                if (shouldReset) {
                    mc.theWorld.setBlockState(blockPos, Blocks.air.defaultState)
                    shouldReset = false
                } else mc.theWorld.setBlockState(BlockPos(blockPos), Blocks.glass.defaultState)
            }
        }
    }




    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        if (mc.currentScreen is EditHudGUI) return

        mc.entityRenderer.setupOverlayRendering()
        hudElements.forEach {
            if (!it.shouldRender()) return@forEach
            GlStateManager.pushMatrix()
            GlStateManager.translate(it.x.toFloat(), it.y.toFloat(), 0f)
            GlStateManager.scale(it.scale, it.scale, 1.0)
            it.render()
            GlStateManager.popMatrix()
        }
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


    fun KeyBind(name: String) = KeyBinding(name, Keyboard.KEY_NONE,"Neion Keybinds")

    val peekBind = KeyBind("Peek Rooms/Names")
    val petsBind = KeyBind("Pets command")
    val tradesBind = KeyBind("Trades command")
    val equipmentBind = KeyBind("Equipment command")
    val wardrobeBind = KeyBind("Wardrobe command")
    val autoRunBind = KeyBind("Autorun")
    val autoSellBind = KeyBind("Auto Sell")

    @SubscribeEvent
    fun onKey(e: InputEvent.KeyInputEvent) {
        if (tradesBind.isPressed) TextUtils.sendCommand("trades")
        if (equipmentBind.isPressed) TextUtils.sendCommand("equipment")
        if (petsBind.isPressed) TextUtils.sendCommand("pets")
        if (wardrobeBind.isPressed) TextUtils.sendCommand("wardrobe")
        if (autoRunBind.isPressed) KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, true)
        if (autoSellBind.isPressed) {
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
    }

    
    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        if ((e.packet as? S2APacketParticles)?.particleType?.particleID.equalsOneOf(25,34)) e.isCanceled = true
    }

    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (e.message?.unformattedText == "[BOSS] The Watcher: I'm impressed.") e.message = ChatComponentText("[BOSS] The Watcher: I'm depressed.")
        if (e.message.unformattedText.equalsOneOf("Oops! You stepped on the wrong block!", "Don't move diagonally! Bad!")) shouldReset = true
    }

    fun saveBackup() {
        val file = File(Neion.modDir, "extrasConfigBackup.json")
        if (file.length() < lastLength) {
            TextUtils.info("YOUR CONFIG FUCKED")
            return
        }
        file.bufferedWriter().use { it.write(gson.toJson(extraRooms)) }
        lastLength = file.length()
    }

    @SubscribeEvent
    fun onWorld(e: WorldEvent.Unload) {
        MurderHelper.murder.clear()
        MurderHelper.bowHolder.clear()
        BlazeSolver.blist.clear()
        MurderHelper.wrote = false
        JasperESP.scanning = false
        ChestProfit.notOpened = true
        ChestProfit.nextAble = true
        ChestProfit.timeWait = 0L
        SimonSaysSolver.clickInOrder.clear()
        SimonSaysSolver.cleared = false
        TeleportMazeSolver.map.clear()
        TeleportMazeSolver.rightOne = null
        WeirdosSolver.riddleChest = null
        WeirdosSolver.notYet = true
        LividSolver.rightLivid = null
        inDungeons = false
        Location.dungeonFloor = -1
        inBoss = false
        DungeonChest.entries.forEach(DungeonChest::reset)
        TriviaSolver.triviaAnswer = null
        ExtrasConfig.saveExtras()
        Dungeon.reset()
        saveBackup()
        if (FreeCam.enabled) FreeCam.toggle()
    }

    @SubscribeEvent
    fun onDisconnect(e: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        inSkyblock = false
        inDungeons = false
        Location.dungeonFloor = -1
        inBoss = false
    }
}