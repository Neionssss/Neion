package neion.features

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.utils.Location
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.BlockPos

object CancelInteractions: Module("Cancel Interactions", category = Category.DUNGEON) {

    val onlyDungeons = BooleanSetting("Only in Dungeons")

    init {
        addSettings(onlyDungeons)
    }

    fun shouldCancel(instance: WorldClient, blockPos: BlockPos): Boolean {
        if (onlyDungeons.enabled && !Location.inDungeons) return false
        return enabled && mc.thePlayer?.heldItem?.item == Items.ender_pearl && !instance.getBlockState(blockPos)?.block.equalsOneOf(Blocks.chest, Blocks.trapped_chest, Blocks.stone_button, Blocks.wooden_button, Blocks.air)
    }
}