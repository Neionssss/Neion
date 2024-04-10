package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.Location.dungeonFloor
import neion.utils.Location.inBoss
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.Utils
import neion.utils.Utils.cleanName
import neion.utils.Utils.itemID
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalSolvers {

    var currentTerminal = Terminal.NONE
    private var firstSlot: Slot? = null
    private val shouldClickColor = mutableListOf<Slot>()
    private val shouldClickStart = mutableListOf<Slot>()
    private val ordering = setOf(
        EnumDyeColor.RED,
        EnumDyeColor.ORANGE,
        EnumDyeColor.YELLOW,
        EnumDyeColor.GREEN,
        EnumDyeColor.BLUE
    ).withIndex().associate { (i, c) -> c.metadata to i }
    private var mostCommon = -1

    @SubscribeEvent
    fun onSlotDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (!inBoss || dungeonFloor != 7 || e.container !is ContainerChest) return
        val invSlots = e.container.inventorySlots
        invSlots?.forEach { slot ->
            if (slot.inventory != e.container.lowerChestInventory) return
            val stack = slot.stack ?: return
            currentTerminal = getCurrentTerminal(e.container)

            when (currentTerminal) {
                Terminal.COLORS -> if (Config.colorsSolver) {
                    if (isValidItem(stack, e)) {
                        shouldClickColor.add(slot)
                        if (Config.terminalHelper) {
                            shouldClickColor.first().apply {
                                xDisplayPosition = 0
                                yDisplayPosition = 0
                            }
                        }
                        slot highlight Config.terminalColor.toJavaColor()
                    } else e.isCanceled = true
                }

                Terminal.STARTSWITH -> if (Config.startsWithSolver) {
                    if (isValidItem(stack,e)) {
                        shouldClickStart.add(slot)
                        if (Config.terminalHelper) {
                            shouldClickStart.first().apply {
                                xDisplayPosition = 0
                                yDisplayPosition = 0
                            }
                        }
                        slot.highlight(Config.terminalColor.toJavaColor())
                    } else e.isCanceled = true
                }

                Terminal.NUMBERS -> if (Config.numbersSolver) {
                    if (isValidItem(stack,e)) {
                        RenderUtil.renderText(
                            stack.stackSize.toString(),
                            slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(stack.stackSize.toString()) / 2,
                            slot.yDisplayPosition + 4
                        )
                        e.isCanceled = true
                    }
                }

                Terminal.CORRECTPANES -> if (Config.terminalHelper) {
                    if (!isValidItem(stack,e)) e.isCanceled = true
                    else invSlots.first { it.stack.cleanName().startsWith("Off") }.apply {
                        xDisplayPosition = 0
                        yDisplayPosition = 0
                        highlight(Config.terminalColor.toJavaColor())
                    }
                }

                else -> {}
            }
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (e.container !is ContainerChest) return
        if (Config.numbersSolver && currentTerminal == Terminal.NUMBERS) handleNumbersSolver(e)
        if (Config.rubixSolver && currentTerminal == Terminal.RUBIX) handleRubixSolver(e.container.inventorySlots)
    }


    @SubscribeEvent
    fun onClickSlot(e: GuiContainerEvent.SlotClickEvent) {
        if (Config.terminalPrevent == 0 || currentTerminal == Terminal.NONE || dungeonFloor != 7 || e.container !is ContainerChest) return

        val slot = e.slot ?: return

        if (slot.inventory != e.container.lowerChestInventory) {
            cancelEvent(e)
            return
        }

        val targetMetadata = when (currentTerminal) {
            Terminal.CORRECTPANES -> EnumDyeColor.LIME.metadata
            Terminal.NUMBERS -> firstSlot?.stack?.metadata
            Terminal.RUBIX -> mostCommon
            Terminal.MELODY -> EnumDyeColor.LIME.metadata
            else -> null
        }

        when (currentTerminal) {
            Terminal.CORRECTPANES -> if (slot.stack?.metadata != targetMetadata) cancelEvent(e)
            Terminal.NUMBERS -> if (slot != firstSlot) cancelEvent(e)
            Terminal.STARTSWITH -> if (shouldClickStart.isNotEmpty() && !shouldClickStart.contains(slot)) cancelEvent(e)
            Terminal.COLORS -> if (shouldClickColor.isNotEmpty() && !shouldClickColor.contains(slot)) cancelEvent(e)
            Terminal.RUBIX -> if (slot.stack?.metadata == targetMetadata) cancelEvent(e)
            Terminal.MELODY -> if (targetMetadata != null && (slot.slotIndex % 9 != shouldClickIndex(targetMetadata) || (slot.slotIndex / 9) * 9 + 7 != slot.slotIndex)) cancelEvent(e)
            else -> {}
        }
    }

    private fun shouldClickIndex(targetMetadata: Int) = when (targetMetadata) {
            EnumDyeColor.MAGENTA.metadata -> 1
            else -> 0
        }

    @SubscribeEvent
    fun onTooltip(e: ItemTooltipEvent) {
        if (Config.hideTooltips && dungeonFloor == 7 && e.toolTip != null && currentTerminal != Terminal.NONE) e.toolTip.clear()
        if (Config.priceTooltip) e.toolTip.add("Lowest Price: ${Utils.fn((Utils.fetchEVERYWHERE(e.itemStack.itemID) ?: Utils.fetchBzPrices(Utils.getBooksID(e.itemStack)) ?: return))}")
    }

    // ----------------------------------------------------------

    private fun handleRubixSolver(invSlots: List<Slot>) {
        val grid = invSlots.filter { it.stack.displayName.startsWith("§a", true) }
        mostCommon = ordering.keys.maxByOrNull { c -> grid.count { it.stack.metadata == c } } ?: EnumDyeColor.RED.metadata

        grid.filter { it.stack.metadata != mostCommon }.associateWith { slote ->
            val myIndex = ordering[slote.stack.metadata]!!
            val targetIndex = ordering[mostCommon]!!
            val leftShift = (targetIndex - myIndex + ordering.size) % ordering.size
            val rightShift = (myIndex - targetIndex + ordering.size) % ordering.size
            leftShift to -rightShift
        }.forEach { (slot, rightShift) ->
                RenderUtil.renderText(
                    "${if (rightShift.first > -rightShift.second) rightShift.second else rightShift.first}",
                    slot.xDisplayPosition + 4,
                    slot.yDisplayPosition + 9
                )
            }
    }

    private fun handleNumbersSolver(e: GuiContainerEvent.BackgroundDrawnEvent) {
        val slotOrder = HashMap<Int, Int>()
        var neededClick = 0
        val invSlots = e.container.inventorySlots

        for (i in (10..16) + (19..25)) {
            val itemStack = invSlots[i].stack ?: continue
            if (itemStack.item == Item.getItemFromBlock(Blocks.stained_glass_pane) && (itemStack.itemDamage == 14 || itemStack.itemDamage == 5)) {
                if (itemStack.stackSize > neededClick) neededClick = itemStack.stackSize
                slotOrder[itemStack.stackSize - 1] = i
            }
        }

        firstSlot?.apply {
            if (Config.terminalHelper) {
                xDisplayPosition = e.mouseX
                yDisplayPosition = e.mouseY
            }
            highlight(Config.firstNumber.toJavaColor())
        }

        invSlots[slotOrder[neededClick + 1] ?: return] highlight Config.secondNumber.toJavaColor()
        invSlots[slotOrder[neededClick + 2] ?: return] highlight Config.thirdNumber.toJavaColor()
    }

    private fun isValidItem(stack: ItemStack, e: GuiContainerEvent) = !stack.isItemEnchanted && when (currentTerminal) {
        Terminal.COLORS -> stack.unlocalizedName?.contains(EnumDyeColor.entries.find { e.chestName.contains(it.name.replace("_", " ").uppercase()) }?.unlocalizedName!!) == true
        Terminal.STARTSWITH -> stack.cleanName().startsWith(Regex("^What starts with: ['\"](.+)['\"]\\?$").find(e.chestName)?.groupValues?.get(1)!!)
        Terminal.CORRECTPANES -> stack.cleanName().startsWith("Off", true)
        Terminal.NUMBERS -> stack.item == Item.getItemFromBlock(Blocks.stained_glass_pane) && stack.itemDamage == 14
        else -> false
    }

    private fun getCurrentTerminal(containerChest: ContainerChest): Terminal {
        firstSlot = null
        shouldClickColor.clear()
        shouldClickStart.clear()
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

    private fun cancelEvent(e: Event) {
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
