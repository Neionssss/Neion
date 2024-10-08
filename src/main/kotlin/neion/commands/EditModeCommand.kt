/* Originally Made by Aton
* https://github.com/FloppaCoding/FloppaClient
 */

package neion.commands

import neion.features.dungeons.EditMode
import neion.features.dungeons.PreBlocks
import neion.utils.ExtrasConfig
import neion.utils.TextUtils
import net.minecraft.block.Block
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.NumberInvalidException
import net.minecraft.init.Blocks

object EditModeCommand : BaseCommand("editmode", listOf("em")) {

    private val shortcuts = mapOf(
        "glass" to Block.getIdFromBlock(Blocks.glass),
        "wall" to Block.getIdFromBlock(Blocks.cobblestone_wall),
        "bars" to Block.getIdFromBlock(Blocks.iron_bars),
        "brick" to Block.getIdFromBlock(Blocks.brick_block),
        "shc" to Block.getIdFromBlock(Blocks.stained_hardened_clay),
        "ec" to Block.getIdFromBlock(Blocks.ender_chest),
        "gb" to Block.getIdFromBlock(Blocks.gold_block),
        "db" to Block.getIdFromBlock(Blocks.diamond_block),
        "ib" to Block.getIdFromBlock(Blocks.iron_block),
        "cb" to Block.getIdFromBlock(Blocks.coal_block),
        "eb" to Block.getIdFromBlock(Blocks.emerald_block),
        "lb" to Block.getIdFromBlock(Blocks.lapis_block),
        "stair" to Block.getIdFromBlock(Blocks.stone_stairs),
        "stairs" to Block.getIdFromBlock(Blocks.stone_stairs),
        "fence" to Block.getIdFromBlock(Blocks.oak_fence),
        "slab" to Block.getIdFromBlock(Blocks.stone_slab)
    )

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty() || !EditMode.enabled) {
            if (PreBlocks.enabled) {
                EditMode.enabled = !EditMode.enabled
                TextUtils.toggledMessage("Edit mode", EditMode.enabled)
                ExtrasConfig.saveExtras()
                ExtrasConfig.loadExtras()
            } else TextUtils.info("Enable extras before using edit mode!")
        } else {
            val id = shortcuts[args[0].lowercase()]
            if (id != null) {
                EditMode.currentBlockID = id
                TextUtils.info("Set block to: ${Block.getStateById(id).block.localizedName}")
                return
            }

            run{
                val data = args[0].split(":")
                val blockID = data.getOrNull(0)?.toIntOrNull() ?: return@run
                val metadata = data.getOrNull(1)?.toIntOrNull() ?: 0
                val state = Block.getBlockById(blockID).getStateFromMeta(metadata)
                EditMode.currentBlockID = Block.getStateId(state)
                TextUtils.info("Set block to: ${state.block.localizedName}")
                return
            }

            try {
                val block = getBlockByText(player, args[0])
                EditMode.currentBlockID = Block.getIdFromBlock(block)
                TextUtils.info("Set block to: ${block.localizedName}")
            } catch (_: NumberInvalidException) {
                TextUtils.info("Invalid Block Name")
            }
        }
    }
}