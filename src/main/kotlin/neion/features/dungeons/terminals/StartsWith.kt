package neion.features.dungeons.terminals

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.ItemUtils.cleanName
import neion.utils.Location.dungeonFloor
import neion.utils.RenderUtil.highlight
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StartsWith {

    @JvmField
    val shouldClick = mutableListOf<Slot>()

    // Couldn't find continue of this :(
    // https://i.imgur.com/7YTH3PY.png
    @SubscribeEvent
    fun sssssloot(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.startsWithSolver || dungeonFloor != 7 || e.container !is ContainerChest || !e.chestName.contains(TerminalFeatures.termNames[3])) return
        shouldClick.clear()
        for (slot in e.container.inventorySlots) {
            if (slot == mc.thePlayer.inventory) continue
            val titler = Regex("^What starts with: ['\"](.+)['\"]\\?$").find(e.chestName) ?: return
            val stack = slot.stack ?: continue
            if (stack.cleanName().startsWith(titler.groupValues[1]) && !stack.isItemEnchanted) {
                slot highlight Config.terminalColor.toJavaColor()
                shouldClick.add(slot)
            }
        }
    }
}
