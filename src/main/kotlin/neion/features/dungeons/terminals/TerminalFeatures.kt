package neion.features.dungeons.terminals

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.features.dungeons.terminals.RubixSolver.mostCommon
import neion.utils.APIHandler
import neion.utils.ItemUtils.lore
import neion.utils.Location.dungeonFloor
import neion.utils.TextUtils.containsAny
import neion.utils.Utils
import neion.utils.Utils.itemID
import neion.utils.Utils.items
import neion.utils.MathUtil
import neion.utils.RenderUtil
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalFeatures {

    private var pickBlockBind = 0

    val termNames = listOf(
        "Click the button on time!",
        "Correct all the panes!",
        "Click in order!",
        "What starts with:",
        "Select all the",
        "Change all to same color!"
    )

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(e: ItemTooltipEvent) {
        if (Config.hideTooltips && dungeonFloor == 7 && e.toolTip != null && (mc.thePlayer.openContainer as? ContainerChest)?.lowerChestInventory?.displayName?.unformattedText?.containsAny(termNames) == true) e.toolTip.clear()
        if (Config.priceTooltip && APIHandler.auctionData != null && APIHandler.profitData != null) {
            val fetchinger = Utils.fetchEVERYWHERE(e.itemStack.itemID) ?: e.itemStack.lore.getOrNull(0)
                ?.let { Utils.enchantNameToID(it) }?.let { Utils.fetchBzPrices(it) } ?: return
            e.toolTip.add("Lowest Price: ${MathUtil.fn(fetchinger)}")
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onMiddleClick(e: GuiContainerEvent.SlotClickEvent) {
        if (Config.middleClickTerms && dungeonFloor == 7) {
            if (e.container is ContainerChest && e.chestName.containsAny(termNames)) {
                pickBlockBind = mc.gameSettings.keyBindPickBlock.keyCodeDefault
                mc.gameSettings.keyBindPickBlock.keyCode = -100
            } else mc.gameSettings.keyBindPickBlock.keyCode = pickBlockBind
        } else mc.gameSettings.keyBindPickBlock.keyCode = pickBlockBind
    }

    @SubscribeEvent
    fun onClickSlot(e: GuiContainerEvent.SlotClickEvent) {
        if (Config.terminalPrevent == 0 || dungeonFloor != 7 || e.container !is ContainerChest || e.slot == mc.thePlayer.inventory) return
        val slot = e.slot ?: return
        val cn = e.chestName
        if (Config.terminalPrevent == 1 && cn.startsWith(termNames[0])) {
            // https://github.com/appable0/AmbientAddons/blob/master/src/main/kotlin/com/ambientaddons/features/dungeon/terminals/MelodyHelper.kt
            val colors = e.container.lowerChestInventory.items.map { it?.itemDamage }
            val movingPaneIndex = colors.indexOf(EnumDyeColor.LIME.metadata)
            if (colors.indexOf(EnumDyeColor.MAGENTA.metadata) != movingPaneIndex % 9 || (movingPaneIndex / 9) * 9 + 7 != slot.slotIndex) cancelEvent(e)
        }
        if (cn.startsWith(termNames[1]) && slot.stack?.metadata == EnumDyeColor.LIME.metadata) cancelEvent(e)
        if (cn.startsWith(termNames[2]) && (slot.slotNumber != NumbersSolver.slotOrder[NumbersSolver.neededClick])) cancelEvent(e)
        if (cn.startsWith(termNames[3])) if (StartsWith.shouldClick.size > 0 && !StartsWith.shouldClick.contains(slot)) cancelEvent(e)
        if (cn.startsWith(termNames[4])) if (ColorsSolver.shouldClick.size > 0 && !ColorsSolver.shouldClick.contains(slot)) cancelEvent(e)
        if (cn.startsWith(termNames[5])) if (slot.stack?.metadata == mostCommon) e.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onDrawSlot(e: GuiContainerEvent.DrawSlotEvent) {
        if (dungeonFloor != 7 || e.container !is ContainerChest || !e.chestName.containsAny(termNames)) return
        val slot = e.slot
        val cn = e.chestName
        if (cn.startsWith(termNames[1]) && slot.stack?.metadata == EnumDyeColor.LIME.metadata) e.isCanceled = true
        if (Config.numbersSolver && cn.startsWith(termNames[2]) && slot.stack?.item == Item.getItemFromBlock(Blocks.stained_glass_pane) && slot.stack.itemDamage == 14) {
            RenderUtil.renderText(slot.stack.stackSize.toString(), slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(slot.stack.stackSize.toString()) / 2, slot.yDisplayPosition + 4)
            e.isCanceled = true
        }
        if (cn.startsWith(termNames[5]) && slot.stack?.metadata == mostCommon) e.isCanceled = true
    }


    // ----------------------------------------------------------
    private fun cancelEvent(e: GuiContainerEvent.SlotClickEvent) {
        e.isCanceled = true
        mc.thePlayer.playSound("random.pop", 1f, 0f)
    }
}
