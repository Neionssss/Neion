package neion.features

import neion.events.GuiContainerEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object ExperimentsSolver: Module("Experiments Solver", category = Category.GENERAL) {

    val order = mutableListOf<Slot>()
    val order2 = mutableListOf<Pair<Slot,String>>()
    val pairs = mutableListOf<Pair<Slot,String>>()
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
            if (!canyes) {
                if (!cCleared) {
                    cCleared = true
                    order.clear()
                }
                if (slot.stack?.item == Item.getItemFromBlock(Blocks.stained_hardened_clay)) order.add(slot)
            } else if (order.isNotEmpty()) {
                cCleared = false
            }
        } else if (e.chestName.startsWith("Superpairs (")) {
            for (i in 11..44) {
                if (slot.slotIndex == i && slot.stack?.item?.equalsOneOf(Item.getItemFromBlock(Blocks.stained_glass), Item.getItemFromBlock(Blocks.stained_glass_pane)) == false && !slot.stack?.cleanName()?.contains("Click Any")!!) pairs.add(Pair(slot, slot.stack?.displayName!!))
                if (pairs.isNotEmpty()) pairs.forEach {
                    it.first.stack?.setStackDisplayName(it.second)
                }
            }
        } else if (e.chestName.startsWith("Ultrasequencer (")) {
            if (!canyes) {
                if (!cleared) {
                    cleared = true
                    order2.clear()
                }
                for (w in 0..15) {
                    if (slot.stack?.displayName?.contains(w.toString()) == true) order2.add(
                        Pair(
                            slot,
                            slot.stack?.displayName!!
                        )
                    )
                }
            } else if (order2.isNotEmpty()) {
                cleared = false
                order2.forEach {
                    RenderUtil.renderText(
                        it.second,
                        it.first.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(it.second) / 2,
                        it.first.yDisplayPosition + 4
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onBackGroundDrawn(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (e.container !is ContainerChest) return
        if (e.chestName.startsWith("Ultrasequencer (") && order2.isNotEmpty()) {
            order2.forEachIndexed { i, it ->
                val color = if (i == 0) Color.green else if (i == 1) Color.yellow else Color.red
                it.first highlight color
            }
        }
        if (e.chestName.startsWith("Chronomatron (") && order.isNotEmpty()) {
            order.first() highlight Color.green
            order[1] highlight Color.green
            order[2] highlight Color.green
        }
    }

    @SubscribeEvent
    fun onClick(e: GuiContainerEvent.SlotClickEvent) {
        if (e.container !is ContainerChest) return
        if (e.chestName.startsWith("Chronomatron (") && order.isNotEmpty() && e.slot.equalsOneOf(order[0], order[1], order[2])) {
            order.removeAll(setOf(order[0], order[1], order[2]))
        }
        if (e.chestName.startsWith("Ultrasequencer (") && order2.isNotEmpty()) order2.removeFirst()
    }

    @SubscribeEvent
    fun onTooltip(e: ItemTooltipEvent) {
        val chest = mc.thePlayer.openContainer as? ContainerChest ?: return
        if (chest.lowerChestInventory?.displayName?.unformattedText?.startsWith("Ultrasequencer (")!!) e.toolTip.clear()
    }

}