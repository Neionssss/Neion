package neion.features

import neion.events.GuiContainerEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.gui.Gui
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

    private val chronomatronOrder = ArrayList<Pair<Int, String>>(28)
    var clicks = 0
    val order2 = HashMap<Slot,String>()
    val pairs = HashMap<Slot,String>()
    var cleared = false
    var cCleared = false

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        chronomatronOrder.clear()
        order2.clear()
        pairs.clear()
        cleared = false
        cCleared = false
        clicks = 0
    }

    private fun getColor(index: Int) = when (index) {
        0 -> Color.green.rgb
        1 -> Color.yellow.rgb
        2 -> Color.red.rgb
        else -> 0xffffff
    }

    @SubscribeEvent
    fun onDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (e.container !is ContainerChest) return
        val slot = e.slot
        if (slot.inventory != e.container.lowerChestInventory) return
        val clockSlot = e.container.lowerChestInventory.sizeInventory - 5
        val clockInSlot = e.container.inventorySlots[clockSlot]?.stack?.item == Items.clock
        val x = slot.xDisplayPosition
        val y = slot.yDisplayPosition
        if (e.chestName.startsWith("Chronomatron (")) {
            if (!clockInSlot) {
                if (!cCleared) {
                    cCleared = true
                    chronomatronOrder.clear()
                    clicks = 0
                }
                if (slot.stack?.isItemEnchanted == true) chronomatronOrder.add(Pair(slot.slotNumber,slot.stack?.displayName!!))
            } else if (chronomatronOrder.isNotEmpty()) {
                cCleared = false
                if (e.chestName.startsWith("Chronomatron (")) {
                    (0..2).find {
                        chronomatronOrder.size > clicks + it && slot.stack?.displayName == chronomatronOrder[clicks + it].second
                    }?.let { Gui.drawRect(x, y, x + 16, y + 16, getColor(it)) }
                }
            }
        } else if (e.chestName.startsWith("Superpairs (")) {
            for (i in 9..44) {
                if (slot.slotIndex == i && slot.stack?.item?.equalsOneOf(Item.getItemFromBlock(Blocks.stained_glass), Item.getItemFromBlock(Blocks.stained_glass_pane)) == false && !slot.stack?.cleanName()?.contains("Click Any")!!) pairs[slot] = slot.stack?.displayName!!
                if (pairs.isNotEmpty()) pairs.entries.find { slot == it.key }?.let {
                    it.key.stack?.setStackDisplayName(it.value)
                }
            }
        } else if (e.chestName.startsWith("Ultrasequencer (")) {
            if (!clockInSlot) {
                if (!cleared) {
                    cleared = true
                    order2.clear()
                }
                for (w in 0..15) {
                    if (slot.stack?.displayName?.contains(w.toString()) == true) order2[slot] = slot.stack?.displayName!!
                }
            } else if (order2.isNotEmpty()) {
                cleared = false
                order2.entries.find { slot == it.key }?.let {
                    RenderUtil.renderText(
                        it.value,
                        x + 9 - mc.fontRendererObj.getStringWidth(it.value) / 2,
                        y + 4
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onBackGroundDrawn(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (e.container !is ContainerChest) return
        if (e.chestName.startsWith("Ultrasequencer (") && !cleared) {
            order2.entries.sortedBy { it.key.stack?.stackSize }.reversed().forEachIndexed { i, it ->
                val color = if (i == 0) Color.green else if (i == 1) Color.yellow else Color.red
                e.container.inventorySlots[it.key.slotIndex] highlight color
            }
        }
    }

    @SubscribeEvent
    fun onClick(e: GuiContainerEvent.SlotClickEvent) {
        if (e.container !is ContainerChest) return
        if (e.chestName.startsWith("Chronomatron (") && !cCleared && e.slot?.slotIndex.equalsOneOf(chronomatronOrder[0], chronomatronOrder[1], chronomatronOrder[2])) {
            clicks++
        }
        val firstUlt = order2.entries.sortedBy { it.key.stack?.displayName?.toInt() }.reversed().first().key
        if (e.chestName.startsWith("Ultrasequencer (") && !cleared && e.slot == firstUlt) order2.remove(firstUlt)
    }

    @SubscribeEvent
    fun onTooltip(e: ItemTooltipEvent) {
        val chest = mc.thePlayer.openContainer as? ContainerChest ?: return
        if (chest.lowerChestInventory?.displayName?.unformattedText?.startsWith("Ultrasequencer (")!!) e.toolTip.clear()
    }

}