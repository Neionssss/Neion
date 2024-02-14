package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.Location.inDungeons
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.BlockPos

/**
 * Cancels block interactions to allow for items to be used.
 *
 * @author Aton
 */
object CancelInteract {

    fun shouldPriotizeAbilityHook(instance: WorldClient, blockPos: BlockPos): Boolean {
        return Config.cancelInteractions && inDungeons && mc.thePlayer.heldItem?.item == Items.ender_pearl && !setOf(
            Blocks.lever,
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.stone_button,
            Blocks.wooden_button,
            Blocks.air
        ).contains(instance.getBlockState(blockPos).block)
    }
}