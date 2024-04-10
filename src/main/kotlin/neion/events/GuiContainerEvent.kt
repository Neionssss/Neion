package neion.events

import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
open class GuiContainerEvent(val container: Container) : DebugEvent() {
    val chestName: String by lazy {
        if (container !is ContainerChest) error("Container is not a chest")
        return@lazy container.lowerChestInventory.displayName.unformattedText.trim()
    }
    class BackgroundDrawnEvent(container: Container, val mouseX: Int, val mouseY: Int) : GuiContainerEvent(container)
    class DrawSlotEvent(container: Container, val slot: Slot) : GuiContainerEvent(container)
    class SlotClickEvent(container: Container, val slot: Slot?, val slotId: Int, val clickedButton: Int, val clickType: Int) : GuiContainerEvent(container)
}