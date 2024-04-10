package neion.features.dungeons

import neion.Config
import neion.events.GuiContainerEvent
import neion.utils.RenderUtil.highlight
import neion.utils.Utils
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.lore
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Croesus {


    @SubscribeEvent
    fun onDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.croesus || e.container !is ContainerChest || !e.chestName.startsWith("Croesus") || !Utils.getArea().contains("Dungeon Hub")) return
        val stack = e.slot.stack ?: return
        val lore = stack.lore
        if (!stack.item.equalsOneOf(Items.skull, Items.arrow)) return
        if (lore.none { it == "§8No Chests Opened!" }) e.isCanceled = true
        else if (lore.any { it.startsWith("§8Opened Chest: ") && Config.showKeyChests }) e.slot.highlight(Color.yellow)
    }
}

