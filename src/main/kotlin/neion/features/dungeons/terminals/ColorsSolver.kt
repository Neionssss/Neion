package neion.features.dungeons.terminals

import neion.Config
import neion.Neion
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.Location.dungeonFloor
import neion.utils.RenderUtil.highlight
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemBlock
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ColorsSolver {

    @JvmField
    val shouldClick = mutableListOf<Slot>()

    @SubscribeEvent
    fun onSlotdrraaww(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.colorsSolver || dungeonFloor != 7 || e.container !is ContainerChest || !e.chestName.contains(TerminalFeatures.termNames[4])) return
        shouldClick.clear()
        for (slot in e.container.inventorySlots) {
            if (slot == mc.thePlayer.inventory) continue
            val promptColor = EnumDyeColor.entries.find { e.chestName.contains(it.getName().replace("_", " ").uppercase()) }?.unlocalizedName ?: return
            e.isCanceled = true
            if (slot.stack?.unlocalizedName?.contains(promptColor)!! && slot.hasStack && !slot.stack?.isItemEnchanted!!) {
                slot highlight Config.terminalColor.toJavaColor()
                shouldClick.add(slot)
            }
        }
    }
}
