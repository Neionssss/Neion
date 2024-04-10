package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.Location.inBoss
import neion.utils.TextUtils
import neion.utils.Utils.chest
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.fetchBzPrices
import neion.utils.Utils.fetchEVERYWHERE
import neion.utils.Utils.getBooksID
import neion.utils.Utils.getChestPrice
import neion.utils.Utils.getEssenceValue
import neion.utils.Utils.itemID
import neion.utils.Utils.lore
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DungeonChestProfit {


    var canOpen = false
    private var timeWait = 0L
    var noobmen = HashSet<Entity>()
    var highestPrice = 0
    var highestPriceChest: Entity? = null

    // https://i.imgur.com/k7mgy9U.png
    @SubscribeEvent
    fun onNo(e: ClientTickEvent) {
        if (!inBoss) return
        val inv = mc.currentScreen?.chest
            if (Config.chestOpener) {
                mc.theWorld?.loadedEntityList?.filter {
                    it is EntityArmorStand && it.getEquipmentInSlot(4)?.cleanName().equalsOneOf(
                        "Wooden Plank Chest",
                        "Golden Skull",
                        "Chest Diamond Right",
                        "Chest Emerald Left",
                        "Chest Obsidian BottomLeft",
                        "Chest Bedrock (Front Bottom Right)"
                    )
                }?.forEach { ent ->
                    if (!noobmen.contains(ent) && mc.thePlayer.getDistanceToEntity(ent) < 20 && System.currentTimeMillis() - timeWait > 100) {
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, ent)
                        timeWait = System.currentTimeMillis()
                        noobmen.add(ent)
                        val chestType = DungeonChest.entries.find { it.displayText == inv?.lowerChestInventory?.displayName?.unformattedText?.trim() } ?: return
                        chestType.value = 0
                        chestType.price = getChestPrice((inv?.getSlot(31)?.stack ?: return).lore)
                        if (Config.chestProfit) canOpen = true
                        chestType.value += calculateItemProfit(inv.inventorySlots)
                        if (chestType.profit > highestPrice) {
                            highestPrice = chestType.profit
                            highestPriceChest = ent
                        }
                        mc.thePlayer.closeScreen()
                    }
                }
                if (highestPriceChest != null && System.currentTimeMillis() - timeWait > 1250) {
                    TextUtils.info(highestPrice.toString())
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, highestPriceChest)
                }
            }
        }
    private fun calculateItemProfit(itemStack: MutableList<Slot>): Int {
        var price = 0
        for (i in 9..17) price += fetchEVERYWHERE(itemStack[i].stack.itemID) ?: fetchBzPrices(getBooksID(itemStack[i].stack)) ?: getEssenceValue(itemStack[i].stack.displayName)
        return price
    }

    // ----------------------------------------------------

    enum class DungeonChest(var displayText: String) {
        WOOD("Wood Chest"),
        GOLD("Gold Chest"),
        DIAMOND("Diamond Chest"),
        EMERALD("Emerald Chest"),
        OBSIDIAN("Obsidian Chest"),
        BEDROCK("Bedrock Chest");

        var price = 0
        var value = 0
        val profit
            get() = value - price

        fun reset() {
            price = 0
            value = 0
        }
    }
}