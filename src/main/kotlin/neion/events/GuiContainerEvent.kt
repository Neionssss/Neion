package neion.events

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
open class GuiContainerEvent(val gui: GuiContainer, val container: Container) : DebugEvent() {
    val chestName: String by lazy {
        if (container !is ContainerChest) error("Container is not a chest")
        return@lazy (container as? ContainerChest)?.lowerChestInventory?.displayName?.unformattedText?.trim()!!
    }

    class BackgroundDrawnEvent(gui: GuiContainer, container: Container, val mouseX: Int, val mouseY: Int) : GuiContainerEvent(gui, container)

    class DrawSlotEvent(gui: GuiContainer, container: Container, val slot: Slot) : GuiContainerEvent(gui, container)

    class SlotClickEvent(gui: GuiContainer, container: Container, val slot: Slot?, val slotId: Int, val clickedButton: Int, val clickType: Int) : GuiContainerEvent(gui, container)
}