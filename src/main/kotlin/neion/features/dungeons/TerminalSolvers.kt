package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.APIHandler
import neion.utils.ItemUtils.cleanName
import neion.utils.Location.dungeonFloor
import neion.utils.Location.inBoss
import neion.utils.MathUtil
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.Utils
import neion.utils.Utils.itemID
import neion.utils.Utils.items
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSolvers {

    var currentTerminal = Terminal.NONE
    var pickBlockBind = 0
    private val shouldClickColor = mutableListOf<Slot>()
    private val shouldClickStart = mutableListOf<Slot>()
    private val correctMap = mutableListOf<Slot>()
    private val ordering = setOf(
        EnumDyeColor.RED,
        EnumDyeColor.ORANGE,
        EnumDyeColor.YELLOW,
        EnumDyeColor.GREEN,
        EnumDyeColor.BLUE
    ).withIndex().associate { (i, c) -> c.metadata to i }
    private var mostCommon = -1

    @SubscribeEvent
    fun onSlotdrraaww(e: GuiContainerEvent.DrawSlotEvent) {
        if (!inBoss || dungeonFloor != 7 || e.container !is ContainerChest) return
        e.container.inventorySlots?.let { grid ->
            for (slot in grid) {
                if (slot.inventory != e.container.lowerChestInventory) continue
                val stack = slot.stack ?: return
                currentTerminal = getCurrentTerminal(e.container)
                when (currentTerminal) {
                    Terminal.COLORS -> if (Config.colorsSolver) {
                        if (stack.unlocalizedName?.contains(EnumDyeColor.entries.find { e.chestName.contains(it.name.replace("_", " ").uppercase()) }?.unlocalizedName!!) == true && !stack.isItemEnchanted) {
                            shouldClickColor.add(slot)
                            if (Config.terminalHelper) {
                                shouldClickColor.first().xDisplayPosition = 0
                                shouldClickColor.first().yDisplayPosition = 0
                            }
                            slot highlight Config.terminalColor.toJavaColor()
                        } else e.isCanceled = true
                    }

                    Terminal.STARTSWITH -> if (Config.startsWithSolver) {
                        if (stack.cleanName().startsWith(Regex("^What starts with: ['\"](.+)['\"]\\?$").find(e.chestName)?.groupValues?.get(1)!!) && !stack.isItemEnchanted) {
                            shouldClickStart.add(slot)
                            if (Config.terminalHelper) {
                                shouldClickStart.first().xDisplayPosition = 0
                                shouldClickStart.first().yDisplayPosition = 0
                            }
                            slot highlight Config.terminalColor.toJavaColor()
                        } else e.isCanceled = true
                    }

                    Terminal.RUBIX -> if (Config.rubixSolver && stack.displayName?.startsWith("§a")!!) {
                        mostCommon = ordering.keys.maxByOrNull { c -> grid.count { it.stack?.metadata == c } } ?: EnumDyeColor.RED.metadata
                        for ((slote, clicks) in grid.filter { stack.metadata != mostCommon }.associateWith {
                            val myIndex = ordering[it?.stack?.metadata]!!
                            val targetIndex = ordering[mostCommon]!!
                            ((targetIndex - myIndex) % ordering.size + ordering.size) % ordering.size to -((myIndex - targetIndex) % ordering.size + ordering.size) % ordering.size
                        }) RenderUtil.renderText("${if (clicks.first > -clicks.second) clicks.second else clicks.first}", slote.xDisplayPosition + 4, slote.yDisplayPosition + 9)
                        if (stack.metadata == mostCommon) e.isCanceled = true
                    }

                    Terminal.NUMBERS -> {
                        if (Config.numbersSolver && stack.item == Item.getItemFromBlock(Blocks.stained_glass_pane) && stack.itemDamage == 14) {
                            RenderUtil.renderText(
                                stack.stackSize.toString(),
                                slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(stack.stackSize.toString()) / 2,
                                slot.yDisplayPosition + 4
                            )
                            e.isCanceled = true
                        }
                    }

                    Terminal.CORRECTPANES -> if (Config.terminalHelper) {
                        if (stack.cleanName().startsWith("Off", true)) correctMap.add(slot) else e.isCanceled = true
                        if (correctMap.isNotEmpty()) {
                            correctMap.first().xDisplayPosition = 0
                            correctMap.first().yDisplayPosition = 0
                            correctMap.first() highlight Config.terminalColor.toJavaColor()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Config.numbersSolver || e.container !is ContainerChest || e.chestName != "Click in order!") return
        val invSlots = e.container.inventorySlots
        val slotOrder = HashMap<Int, Int>()
        var neededClick = 0
        for (i in (10..16) + (19..25)) {
            val itemStack = invSlots[i].stack ?: continue
            if (itemStack.item != Item.getItemFromBlock(Blocks.stained_glass_pane) || itemStack.itemDamage != 14 && itemStack.itemDamage != 5) continue
            if (itemStack.itemDamage == 5 && itemStack.stackSize > neededClick) neededClick = itemStack.stackSize
            slotOrder[itemStack.stackSize - 1] = i
        }
        val firstSlot = invSlots[slotOrder[neededClick] ?: return]
        if (Config.terminalHelper) {
            firstSlot.xDisplayPosition = 0
            firstSlot.yDisplayPosition = 0
        }
        firstSlot highlight Config.firstNumber.toJavaColor()
        invSlots[slotOrder[neededClick + 1] ?: return] highlight Config.secondNumber.toJavaColor()
        invSlots[slotOrder[neededClick + 2] ?: return] highlight Config.thirdNumber.toJavaColor()

    }
    @SubscribeEvent
    fun onClickSlot(e: GuiContainerEvent.SlotClickEvent) {
        if (Config.terminalPrevent != 0 && currentTerminal != Terminal.NONE && dungeonFloor == 7 && e.container is ContainerChest) {
            val slot = e.slot ?: return
            if (slot.inventory != e.container.lowerChestInventory) cancelEvent(e)
            pickBlockBind = mc.gameSettings.keyBindPickBlock.keyCodeDefault
            mc.gameSettings.keyBindPickBlock.keyCode = -100

            when (currentTerminal) {
                Terminal.CORRECTPANES -> if (slot.stack?.metadata == EnumDyeColor.LIME.metadata) cancelEvent(e)
                Terminal.NUMBERS -> if (slot.stack?.metadata != EnumDyeColor.RED.metadata || slot.stack?.stackSize != e.container.lowerChestInventory.items.count { it?.metadata == EnumDyeColor.LIME.metadata } + 1) cancelEvent(e)

                Terminal.STARTSWITH -> if (shouldClickStart.size > 0 && !shouldClickStart.contains(slot)) cancelEvent(e)
                Terminal.COLORS -> if (shouldClickColor.size > 0 && !shouldClickColor.contains(slot)) cancelEvent(e)
                Terminal.RUBIX -> if (slot.stack?.metadata == mostCommon) cancelEvent(e)
                Terminal.MELODY -> if (Config.terminalPrevent == 1) {
                    val colors = e.container.lowerChestInventory.items.map { it?.itemDamage }
                    val movingPaneIndex = colors.indexOf(EnumDyeColor.LIME.metadata)
                    if (movingPaneIndex % 9 != colors.indexOf(EnumDyeColor.MAGENTA.metadata) || (movingPaneIndex / 9) * 9 + 7 != slot.slotIndex) cancelEvent(e)
                }

                else -> {}
            }
        } else mc.gameSettings.keyBindPickBlock.keyCode = pickBlockBind
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(e: ItemTooltipEvent) {
        if (Config.hideTooltips && dungeonFloor == 7 && e.toolTip != null && currentTerminal != Terminal.NONE) e.toolTip.clear()
        if (Config.priceTooltip && APIHandler.auctionData != null && APIHandler.profitData != null) {
            val fetchinger = Utils.fetchEVERYWHERE(e.itemStack.itemID) ?: Utils.fetchBzPrices(Utils.getBooksID(e.itemStack))?: return
            e.toolTip.add("Lowest Price: ${MathUtil.fn(fetchinger)}")
        }
    }

    // ----------------------------------------------------------

    private fun getCurrentTerminal(containerChest: ContainerChest): Terminal {
        shouldClickColor.clear()
        shouldClickStart.clear()
        correctMap.clear()
        val chestName = containerChest.lowerChestInventory?.displayName?.unformattedText ?: return Terminal.NONE
        return when {
            chestName == "Click in order!" -> Terminal.NUMBERS
            chestName == "Correct all the panes!" -> Terminal.CORRECTPANES
            chestName.startsWith("What starts with: ") -> Terminal.STARTSWITH
            chestName.startsWith("Select all the ") -> Terminal.COLORS
            chestName == "Click the button on time!" -> Terminal.MELODY
            chestName == "Change all to same color!" -> Terminal.RUBIX
            else -> Terminal.NONE
        }
    }

    private fun cancelEvent(e: GuiContainerEvent.SlotClickEvent) {
        e.isCanceled = true
        mc.thePlayer.playSound("random.pop", 1f, 0f)
    }
    enum class Terminal {
        STARTSWITH,
        COLORS,
        NUMBERS,
        RUBIX,
        MELODY,
        CORRECTPANES,
        NONE
    }
}
