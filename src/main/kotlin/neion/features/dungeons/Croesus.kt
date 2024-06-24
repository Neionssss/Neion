package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.ItemUtils.lore
import neion.utils.RenderUtil.highlight
import neion.utils.TextUtils.containsAny
import neion.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object Croesus {

    val setSome = arrayListOf<dddswsdad>()
    private var slottter = HashSet<Int>()
    private val chests = listOf(
        "Wood Chest",
        "Gold Chest",
        "Diamond Chest",
        "Emerald Chest",
        "Obsidian Chest",
        "Bedrock Chest"
    )


    @SubscribeEvent
    fun onDraw(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.croesus || e.container !is ContainerChest || !Utils.getArea().contains("Dungeon Hub")) return
        val stack = e.slot.stack ?: return
        val lore = stack.lore
        val cn = e.chestName
        if (!stack.item.equalsOneOf(Items.skull, Items.arrow, ItemBlock.getItemFromBlock(Blocks.chest))) return
        if (cn.startsWith("Croesus")) {
            setSome.clear()
            slottter.clear()
            if (lore.none { it == "§8No Chests Opened!" }) e.isCanceled = true else if (Config.autoCroesus) slottter.add(e.slot.slotNumber)
            if (lore.any { it.startsWith("§8Opened Chest: ") && Config.showKeyChests }) e.slot highlight Color.yellow
            if (Config.autoCroesus && System.currentTimeMillis() - Utils.lastClickTime > Config.autoCroesusDelay && slottter.isNotEmpty()) Utils.clickSlot(slottter.first())
        }
        if (cn.contains("The Catacombs")) {
            setSome.clear()
            e.container.inventorySlots.forEach {
                if (it.stack?.displayName?.containsAny(chests) == true) {
                    // https://i.imgur.com/3ZPLxXJ.png
                    it.stack.lore.forEach { line ->
                        if (line.contains("Dungeon Chest Key")) return
                        setSome.add(dddswsdad(it, (Utils.fetchEVERYWHERE(Utils.getIdFromName(line)) ?: Utils.getEssenceValue(line)) - Utils.getChestPrice(it.stack.lore)))
                    }
                }
            }
            if (setSome.isNotEmpty()) {
                setSome.sortWith { it1, it2 -> it1.pricer.compareTo(it2.pricer) }
                setSome.last().slot highlight Color.red
                if (Config.autoCroesus && System.currentTimeMillis() - Utils.lastClickTime > Config.autoCroesusDelay) Utils.clickSlot(setSome.last().slot.slotNumber)
            }
        }
        if (Config.autoCroesus && cn.containsAny(chests) && System.currentTimeMillis() - Utils.lastClickTime > Config.autoCroesusDelay) Utils.clickSlot(31)
    }


    @SubscribeEvent
    fun onRender(e: ClientTickEvent) {
        if (!Config.croesus || !Config.autoCroesus || !Utils.getArea().contains("Dungeon Hub")) return
        mc.theWorld.loadedEntityList.filter { it.displayName.unformattedText.contains("TREASURES") }.forEach {
            if (it.displayName.unformattedText.contains("TREASURES") && (mc.thePlayer.openContainer as? ContainerChest) == null) {
                if (mc.thePlayer.getDistanceToEntity(it) < 3 && System.currentTimeMillis() - Utils.lastClickTime > Config.autoCroesusDelay) {
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                    Utils.lastClickTime = System.currentTimeMillis()
                }
            }
        }
    }



    data class dddswsdad(var slot: Slot, var pricer: Int)
}

