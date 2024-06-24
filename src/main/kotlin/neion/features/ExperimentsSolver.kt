package neion.features

import neion.events.GuiContainerEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object ExperimentsSolver: Module("Experiments Solver", category = Category.GENERAL) {

    val order: ArrayList<Slot> = arrayListOf()
    val order2: ArrayList<Slot> = arrayListOf()
    val pairs = arrayListOf<Slot>()
    var cleared = false
    var cCleared = false

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        order.clear()
        order2.clear()
        pairs.clear()
        cleared = false
        cCleared = false
    }

    @SubscribeEvent
    fun onDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (e.container !is ContainerChest) return
        val slot = e.slot
        if (slot.inventory != e.container.lowerChestInventory) return
        val clockSlot = e.container.lowerChestInventory.sizeInventory - 5
        val canyes = e.container.inventorySlots[clockSlot]?.stack?.item == Items.clock
        if (e.chestName.startsWith("Chronomatron (")) {
            if (canyes) {
                if (order.isNotEmpty()) {
                    cCleared = false
                    order.first() highlight Color.green
                    order[1] highlight Color.green
                    order[2] highlight Color.green
                }
            } else {
                if (cCleared) {
                    cCleared = true
                    order.clear()
                }
                if (slot.stack?.item == Item.getItemFromBlock(Blocks.stained_hardened_clay)) order.add(slot)
            }
        } else if (e.chestName.startsWith("Superpairs (")) {
            for (i in 11..44) {
                if (slot.slotIndex == i && slot.stack?.item?.equalsOneOf(Item.getItemFromBlock(Blocks.stained_glass), Item.getItemFromBlock(Blocks.stained_glass_pane)) == false && !slot.stack?.cleanName()?.contains("Click Any")!!) pairs.add(slot)
                if (pairs.isNotEmpty()) pairs.forEach {
                    slot.stack.setStackDisplayName(it.stack.displayName)
                }
            }
        } else if (e.chestName.startsWith("Ultrasequencer (")) {
            if (!canyes) {
                if (!cleared) {
                    cleared = true
                    order2.clear()
                }
                if (slot.stack?.displayName.containsAny(
                        "1",
                        "2",
                        "3",
                        "4",
                        "5",
                        "6",
                        "7",
                        "8",
                        "9",
                        "10",
                        "11",
                        "12",
                        "13",
                        "14",
                        "15"
                    )
                ) order2.add(slot)
            } else if (order2.isNotEmpty()) {
                    cleared = false
                    order2.forEach {
                        RenderUtil.renderText(it.stack?.displayName!!, it.xDisplayPosition + 9,it.yDisplayPosition + 4)
                    }
                }
        }
    }

    @SubscribeEvent
    fun onClick(e: GuiContainerEvent.SlotClickEvent) {
        if (e.container !is ContainerChest) return
        if (e.chestName.contains("Chronomatron (") && order.isNotEmpty() && e.slot.equalsOneOf(order[0], order[1], order[2])) {
            order.removeAll(setOf(order[0], order[1], order[2]))
        }
    }

}