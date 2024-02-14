/*
 * Credit Skytils
 * https://github.com/Skytils/SkytilsMod
 */
package neion.features.dungeons.terminals

import neion.Config
import neion.events.GuiContainerEvent
import neion.utils.Location.dungeonFloor
import neion.utils.RenderUtil
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RubixSolver {
    private val ordering = setOf(EnumDyeColor.RED, EnumDyeColor.ORANGE, EnumDyeColor.YELLOW, EnumDyeColor.GREEN, EnumDyeColor.BLUE).withIndex().associate { (i, c) -> c.metadata to i }
    var mostCommon = -1

    @SubscribeEvent
    fun onBackgroundDrawn(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Config.rubixSolver || dungeonFloor != 7 || e.container !is ContainerChest || e.chestName != "Change all to same color!") return
        val grid = e.container.inventorySlots.filter { it.inventory == e.container.lowerChestInventory && it.stack?.displayName?.startsWith("§a") == true}
        mostCommon = ordering.keys.maxByOrNull { c -> grid.count { it.stack?.metadata == c } } ?: EnumDyeColor.RED.metadata
        val mapping = grid.filter { it.stack.metadata != mostCommon }.associateWith { slot ->
            val myIndex = ordering[slot.stack.metadata]!!
            val targetIndex = ordering[mostCommon]!!
            val normalCycle = ((targetIndex - myIndex) % ordering.size + ordering.size) % ordering.size
            val otherCycle = -((myIndex - targetIndex) % ordering.size + ordering.size) % ordering.size
            normalCycle to otherCycle
        }
        for ((slot, clicks) in mapping) {
            RenderUtil.renderText(
                "${if (clicks.first > -clicks.second) clicks.second else clicks.first}",
                slot.xDisplayPosition + 4,
                slot.yDisplayPosition + 9)
        }
    }
}