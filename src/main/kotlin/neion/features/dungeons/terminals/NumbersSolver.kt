package neion.features.dungeons.terminals

import neion.Config
import neion.events.GuiContainerEvent
import neion.utils.RenderUtil.highlight
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NumbersSolver {

    val slotOrder = HashMap<Int, Int>()
    var neededClick = 0

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        slotOrder.clear()
        neededClick = 0
    }

    @SubscribeEvent
    fun onBackgroundDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.numbersSolver || e.container !is ContainerChest || e.chestName != "Click in order!") return
        val invSlots = e.container.inventorySlots
        for (i in (10..25)) {
            val itemStack = invSlots[i].stack ?: continue
            if (itemStack.item != Item.getItemFromBlock(Blocks.stained_glass_pane) || itemStack.itemDamage != 5) continue
            if (itemStack.stackSize > neededClick) neededClick = itemStack.stackSize
            slotOrder[itemStack.stackSize - 1] = i
            invSlots[slotOrder[neededClick] ?: return] highlight Config.firstNumber.toJavaColor()
            invSlots[slotOrder[neededClick + 1] ?: return] highlight Config.secondNumber.toJavaColor()
            invSlots[slotOrder[neededClick + 2] ?: return] highlight Config.thirdNumber.toJavaColor()
        }
    }
}