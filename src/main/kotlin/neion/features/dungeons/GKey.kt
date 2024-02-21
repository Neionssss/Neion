package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.ClickEvent
import neion.utils.Location
import net.minecraft.init.Blocks
import net.minecraft.item.ItemPickaxe
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object GKey {

    // There was no "entityarmostandMutableList" unfortunately
    private var last0l = 0L


    /* The time I was unable to make GKey I copied this from
    https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/GhostBlock.kt
    Actually there's nothing to make better, so it's ok for now
     */
    private val blacklist = listOf(
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
    )

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (!Config.GGkey || !Config.GGkeyBind.isActive || mc.ingameGUI.chatGUI.chatOpen) return
        val rt = mc.thePlayer?.rayTrace(Config.GRange.toDouble(), 0.0f)
        if (!blacklist.contains(mc.theWorld.getBlockState(rt?.blockPos).block) && System.currentTimeMillis() - last0l > Config.GDelay) {
            mc.theWorld.setBlockState(rt?.blockPos, Blocks.air.defaultState)
            last0l = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onRightClick(e: ClickEvent.RightClickEvent) {
        if (!Config.rcmGB || !Location.inDungeons || Config.GGkeyBind.isActive || mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val rt = mc.thePlayer?.rayTrace(Config.GRange.toDouble(), 0.0f)
        if (mc.thePlayer.heldItem?.item is ItemPickaxe && !blacklist.contains(mc.theWorld.getBlockState(rt?.blockPos).block)) {
            e.isCanceled = mc.theWorld.setBlockState(rt?.blockPos, Blocks.air.defaultState)
        }
    }
}
