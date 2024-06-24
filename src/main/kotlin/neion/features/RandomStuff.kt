package neion.features

import neion.Config
import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.events.PacketReceiveEvent
import neion.events.PacketSentEvent
import neion.features.dungeons.*
import neion.funnymap.Dungeon
import neion.funnymap.map.ScanUtils
import neion.ui.GuiRenderer
import neion.utils.*
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location.inBoss
import neion.utils.Location.inDungeons
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils.startsWithAny
import neion.utils.Utils.extraAttributes
import neion.utils.Utils.itemID
import neion.utils.Utils.items
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.potion.PotionEffect
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import org.lwjgl.opengl.Display
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.exp
import kotlin.random.Random


object RandomStuff {


    private var lastGive = 0L
    private var last0l = 0L

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
        if (!Config.autoSB || e.type == 2.toByte()) return
        val messagee = e.message.unformattedText
        if (inSkyblock && messagee.startsWith("You were kicked while joining that server!")) {
            TextUtils.info("You have auto SB turned on and will be auto-reconnected in a minute!")
            Timer("ARC", false).schedule(55000) { mc.thePlayer.sendChatMessage("/play sb") }
        } else if (!inSkyblock && messagee.startsWithAny("You were kicked while joining that server!", "You tried to rejoin too fast, please try again in a moment!")) {
            Timer("ARC", false).schedule(10000) { mc.thePlayer.sendChatMessage("/play sb") }
        }
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (Config.noReverse3rdPerson && mc.gameSettings.thirdPersonView == 2) mc.gameSettings.thirdPersonView = 0
        if (Config.ToggleSprint) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
        if (Config.fakeHaste && inDungeons) mc.thePlayer.addPotionEffect(PotionEffect(3, 999999999, 2))
        if (Config.funnyItems) mc.thePlayer.swingProgress = (exp(-Random.nextFloat()) + -Config.itemSwingSpeed)
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (GuiRenderer.titleTicks > 0) GuiRenderer.titleTicks--
        if (e.phase == TickEvent.Phase.START && mc.currentScreen !is GuiChest && TerminalSolvers.currentTerminal != TerminalSolvers.Terminal.NONE) TerminalSolvers.currentTerminal = TerminalSolvers.Terminal.NONE
        Dungeon.onTick()
        Location.onTick()
        BlazeSolver.onTick()
        Display.setTitle(Config.mcTitle)
        if (Config.autoGFS && inDungeons) {
            mc.thePlayer?.inventory?.items?.filter { it?.item == Items.ender_pearl && System.currentTimeMillis() - lastGive > 5000 }
                ?.forEach {
                    if (it != null) {
                        if (it.stackSize > Config.minep) return
                        TextUtils.sendCommand("gfs ender_pearl ${16 - it.stackSize}")
                        lastGive = System.currentTimeMillis()
                    }
                }
        }
        if (Config.GGkey && Config.GGkeyBind.isActive && !mc.ingameGUI.chatGUI.chatOpen) {
            val rt = mc.thePlayer?.rayTrace(Config.GRange.toDouble(), 0.0f)?.blockPos
            //https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/GhostBlock.kt
            if (!listOf(
                    Blocks.acacia_door,
                    Blocks.anvil,
                    Blocks.beacon,
                    Blocks.bed,
                    Blocks.birch_door,
                    Blocks.brewing_stand,
                    Blocks.brown_mushroom,
                    Blocks.chest,
                    Blocks.command_block,
                    Blocks.crafting_table,
                    Blocks.dark_oak_door,
                    Blocks.daylight_detector,
                    Blocks.daylight_detector_inverted,
                    Blocks.dispenser,
                    Blocks.dropper,
                    Blocks.enchanting_table,
                    Blocks.ender_chest,
                    Blocks.furnace,
                    Blocks.hopper,
                    Blocks.jungle_door,
                    Blocks.lever,
                    Blocks.noteblock,
                    Blocks.oak_door,
                    Blocks.powered_comparator,
                    Blocks.powered_repeater,
                    Blocks.red_mushroom,
                    Blocks.standing_sign,
                    Blocks.stone_button,
                    Blocks.trapdoor,
                    Blocks.trapped_chest,
                    Blocks.unpowered_comparator,
                    Blocks.unpowered_repeater,
                    Blocks.wall_sign,
                    Blocks.wooden_button,
                    Blocks.air,
                    Blocks.skull
                ).contains(mc.theWorld.getBlockState(rt).block) && System.currentTimeMillis() - last0l > Config.GDelay) {
                mc.theWorld.setBlockState(rt, Blocks.air.defaultState)
                last0l = System.currentTimeMillis()
            }
        }
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
        when (val packet = e.packet) {
            is S2APacketParticles -> if (packet.particleType?.particleID == 25 && Config.HideEnchantRune || packet.particleType?.particleID == 34 && Config.hideHeartParticles) e.isCanceled = true

            is S2DPacketOpenWindow -> if (Config.autoclose && inDungeons && packet.windowTitle.unformattedText.equalsOneOf("Chest", "Large Chest")) {
                e.isCanceled = true
                mc.netHandler.networkManager.sendPacket(C0DPacketCloseWindow(packet.windowId))
            }

            is C03PacketPlayer -> EtherwarpUtils.sss(packet)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (Config.etherwarpOverlay && mc.thePlayer.isSneaking && mc.thePlayer?.heldItem?.itemID.equalsOneOf("ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID") && mc.thePlayer?.heldItem?.extraAttributes?.hasKey("ethermerge")!!) {
            val (x, y, z) = EtherwarpUtils.traverseVoxels() ?: return
            RenderUtil.drawBlockBox(BlockPos(x, y, z), Config.etherwarpColor.toJavaColor(), true, false, true)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: CheckRenderEntityEvent<*>) {
        if (!Config.itemESP || !inDungeons || inBoss) return
        val entity = e.entity as? EntityItem ?: return
        if (mc.thePlayer.getDistanceSqToEntity(entity) < 200 && listOf(
                        Items.ender_pearl,
                        Items.spawn_egg,
                        Items.potionitem,
                        Items.skull,
                        Items.shears,
                        ItemBlock.getItemFromBlock(Blocks.iron_trapdoor),
                        ItemBlock.getItemFromBlock(Blocks.skull),
                        ItemBlock.getItemFromBlock(Blocks.heavy_weighted_pressure_plate)).any { entity.entityItem.item == it })
            RenderUtil.drawEntityBox(entity, Config.itemColor.toJavaColor(), true, false,true)
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

    fun shouldPriotizeAbilityHook(instance: WorldClient, blockPos: BlockPos) = Config.cancelInteractions && inDungeons && mc.thePlayer.heldItem?.item == Items.ender_pearl && !setOf(
        Blocks.lever,
        Blocks.chest,
        Blocks.trapped_chest,
        Blocks.stone_button,
        Blocks.wooden_button,
        Blocks.air).contains(instance.getBlockState(blockPos).block)

    @SubscribeEvent
    fun onWorld(e: WorldEvent.Unload) {
        MurderHelper.murder.clear()
        MurderHelper.bowHolder.clear()
        MurderHelper.wrote = false
        DungeonChestProfit.canOpen = false
        DungeonChestProfit.notOpened = true
        DungeonChestProfit.noobmen.clear()
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
        TriviaSolver.triviaAnswer = null
        ScanUtils.saveExtras()
        Dungeon.reset()
        if (Config.freeCam) Config.freeCam = false
    }

    @SubscribeEvent
    fun onDisconnect(e: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        inSkyblock = false
        inDungeons = false
        Location.dungeonFloor = -1
        inBoss = false
    }
}