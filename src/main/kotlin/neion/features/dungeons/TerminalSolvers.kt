package neion.features.dungeons

import neion.events.GuiContainerEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.ColorSetting
import neion.ui.clickgui.settings.SelectorSetting
import neion.utils.Location.dungeonFloor
import neion.utils.Location.inBoss
import neion.utils.RenderUtil
import neion.utils.RenderUtil.highlight
import neion.utils.Utils.cleanName
import neion.utils.Utils.items
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object TerminalSolvers: Module("Terminal Solvers", category = Category.DUNGEON) {

    val colorsSolver = BooleanSetting("Colors Solver")
    val startsWithSolver = BooleanSetting("StartsWith Solver")
    val rubixSolver = BooleanSetting("Rubix Solver")
    val numbersSolver = BooleanSetting("Numbers Solver")
    val hideToolTips = BooleanSetting("Hide Tooltips")
    val preventWrong = SelectorSetting("Prevent Wrong Clicks", "OFF", arrayOf("OFF", "ALL", "ALL except Melody"))
    val terminalHelper = BooleanSetting("Terminal Helper", description = "Makes all slots be in one place")
    val terminalColor = ColorSetting("Color", default = Color.red,true)
    val firstColor = ColorSetting("Numbers First Color", default = Color(0,0,200,255))
    val secondColor = ColorSetting("Numbers Second Color", default = Color(0,0,150,255))
    val thirdColor = ColorSetting("Numbers Third Color", default = Color(0,0,100,255))

    init {
        addSettings(colorsSolver,
            startsWithSolver,
            rubixSolver,
            numbersSolver,
            hideToolTips,
            preventWrong,
            terminalHelper,
            terminalColor,
            firstColor,
            secondColor,
            thirdColor
        )
    }

    var currentTerminal = Terminal.NONE
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
    fun onSlotdrraaww(e: GuiContainerEvent.DrawSlotEvent) {
        if (!inBoss || dungeonFloor != 7 || e.container !is ContainerChest) return
        val slot = e.slot
        val stack = slot.stack ?: return
        if (slot.inventory != e.container.lowerChestInventory) return
        currentTerminal = getCurrentTerminal(e.container)
        when (currentTerminal) {
            Terminal.COLORS -> if (colorsSolver.enabled) {
                if (stack.unlocalizedName?.contains(EnumDyeColor.entries.find {
                        e.chestName.contains(
                            it.getName().replace("_", " ").uppercase()
                        )
                    }?.unlocalizedName!!) == true && !stack.isItemEnchanted) {
                    shouldClickColor.add(slot)
                    if (terminalHelper.enabled && shouldClickColor.isNotEmpty()) {
                        shouldClickColor.first().xDisplayPosition = 0
                        shouldClickColor.first().yDisplayPosition = 0
                    }
                    slot highlight terminalColor.value
                } else e.isCanceled = true
            }

            Terminal.STARTSWITH -> if (startsWithSolver.enabled) {
                if (stack.cleanName().startsWith(Regex("^What starts with: ['\"](.+)['\"]\\?$").find(e.chestName)?.groupValues?.get(1)!!) && !stack.isItemEnchanted) {
                    shouldClickStart.add(slot)
                    if (terminalHelper.enabled && shouldClickStart.isNotEmpty()) {
                        shouldClickStart.first().xDisplayPosition = 0
                        shouldClickStart.first().yDisplayPosition = 0
                    }
                    slot highlight terminalColor.value
                } else e.isCanceled = true
            }

            Terminal.RUBIX -> if (rubixSolver.enabled) {
                val grid = e.container.inventorySlots.filter {
                    it.inventory == e.container.lowerChestInventory && it.stack?.displayName?.startsWith("§a") == true
                }
                mostCommon = ordering.keys.maxByOrNull { c -> grid.count { it.stack?.metadata == c } }
                    ?: EnumDyeColor.RED.metadata
                val mapping = grid.filter { it.stack.metadata != mostCommon }.associateWith { slote ->
                    val myIndex = ordering[slote.stack.metadata]!!
                    val targetIndex = ordering[mostCommon]!!
                    ((targetIndex - myIndex) % ordering.size + ordering.size) % ordering.size to -((myIndex - targetIndex) % ordering.size + ordering.size) % ordering.size
                }
                for ((slote, clicks) in mapping) {
                    RenderUtil.renderText(
                        "${if (clicks.first > -clicks.second) clicks.second else clicks.first}",
                        slote.xDisplayPosition + 4,
                        slote.yDisplayPosition + 9
                    )
                }
                if (stack.metadata == mostCommon) e.isCanceled = true
            }

            Terminal.NUMBERS -> if (numbersSolver.enabled && stack.item == Item.getItemFromBlock(Blocks.stained_glass_pane) && stack.itemDamage == 14) {
                RenderUtil.renderText(
                    stack.stackSize.toString(),
                    slot.xDisplayPosition + 9 - mc.fontRendererObj.getStringWidth(stack.stackSize.toString()) / 2,
                    slot.yDisplayPosition + 4
                )
                e.isCanceled = true
            }

            Terminal.CORRECTPANES -> if (terminalHelper.enabled) {
                val minePanes = e.container.inventorySlots.filter { it.stack.displayName.contains("Off") }
                if (slot !in minePanes) e.isCanceled = true
                if (minePanes.isNotEmpty()) {
                    minePanes.last().xDisplayPosition = 0
                    minePanes.last().yDisplayPosition = 0
                }
            }

            else -> {}
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(e: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!numbersSolver.enabled || e.container !is ContainerChest || e.chestName != "Click in order!") return
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
        if (terminalHelper.enabled) {
            firstSlot.xDisplayPosition = 0
            firstSlot.yDisplayPosition = 0
        }
        firstSlot highlight firstColor.value
        invSlots[slotOrder[neededClick + 1] ?: return] highlight secondColor.value
        invSlots[slotOrder[neededClick + 2] ?: return] highlight thirdColor.value
    }

    @SubscribeEvent
    fun onClickSlot(e: GuiContainerEvent.SlotClickEvent) {
        if (!preventWrong.isSelected("OFF") && currentTerminal != Terminal.NONE && dungeonFloor == 7 && e.container is ContainerChest) {
            val slot = e.slot ?: return
            if (slot.inventory == mc.thePlayer.inventory) cancelEvent(e)

            when (currentTerminal) {
                Terminal.CORRECTPANES -> if (slot.stack?.metadata == EnumDyeColor.LIME.metadata) cancelEvent(e)
                Terminal.NUMBERS -> if (slot.stack?.metadata != EnumDyeColor.RED.metadata || slot.stack?.stackSize != e.container.lowerChestInventory.items.count { it?.metadata == EnumDyeColor.LIME.metadata } + 1) cancelEvent(e)

                Terminal.STARTSWITH -> if (shouldClickStart.size > 0 && !shouldClickStart.contains(slot)) cancelEvent(e)
                Terminal.COLORS -> if (shouldClickColor.size > 0 && !shouldClickColor.contains(slot)) cancelEvent(e)
                Terminal.RUBIX -> if (slot.stack?.metadata == mostCommon) cancelEvent(e)
                Terminal.MELODY -> if (preventWrong.isSelected("ALL")) {
                    val colors = e.container.lowerChestInventory.items.map { it?.itemDamage }
                    val movingPaneIndex = colors.indexOf(EnumDyeColor.LIME.metadata)
                    if (movingPaneIndex % 9 != colors.indexOf(EnumDyeColor.MAGENTA.metadata) || (movingPaneIndex / 9) * 9 + 7 != slot.slotIndex) cancelEvent(e)
                }

                else -> {}
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(e: ItemTooltipEvent) {
        if (hideToolTips.enabled && dungeonFloor == 7 && e.toolTip != null && currentTerminal != Terminal.NONE) e.toolTip.clear()
    }

    // ----------------------------------------------------------

    private fun getCurrentTerminal(containerChest: ContainerChest): Terminal {
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