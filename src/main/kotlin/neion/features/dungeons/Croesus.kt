package neion.features.dungeons

import neion.events.GuiContainerEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.utils.RenderUtil.highlight
import neion.utils.Utils
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.lore
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Croesus: Module("Croesus Helper", category = Category.DUNGEON) {

    val keyChests = BooleanSetting("Key chests")

    init {
        addSettings(keyChests)
    }

    @SubscribeEvent
    fun onDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (e.container !is ContainerChest || !Utils.getArea().contains("Dungeon Hub") || e.chestName != "Croesus") return
        val stack = e.slot.stack ?: return
        val lore = stack.lore
        if (!stack.item.equalsOneOf(Items.skull, ItemBlock.getItemFromBlock(Blocks.chest))) return
        if (lore.none { it == "§8No Chests Opened!" }) e.isCanceled = true
        if (lore.any { it.startsWith("§8Opened Chest: ") && keyChests.enabled }) e.slot highlight Color.yellow
    }
}
