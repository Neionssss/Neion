package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.ItemUtils.cleanName
import neion.utils.ItemUtils.lore
import neion.utils.Location.inBoss
import neion.utils.Utils
import neion.utils.Utils.chest
import neion.utils.Utils.itemID
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DungeonChestProfit {


    // https://i.imgur.com/k7mgy9U.png
    @SubscribeEvent
    fun onNo(e: ClientTickEvent) {
        if (!inBoss) return
        val inv = mc.currentScreen?.chest
            if (inv == null) {
                if (Config.chestOpener) {
                    mc.theWorld?.loadedEntityList?.filter {
                        it is EntityArmorStand && Chestser.entries.any { w ->
                            it.getEquipmentInSlot(4)?.cleanName() == w.s
                        }
                    }?.forEach { ent ->
                        if (!noobmen.contains(ent) && mc.thePlayer.getDistanceToEntity(ent) < 20 && System.currentTimeMillis() - timeWait > 100) {
                            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, ent)
                            timeWait = System.currentTimeMillis()
                            noobmen.add(ent)
                        }
                        if (System.currentTimeMillis() - timeWait > 400 && notOpened) notOpened = false
                    }
                }
            } else {
                val chestType = DungeonChest.getFromName(inv.lowerChestInventory.displayName.unformattedText.trim()) ?: return
                val sssa = inv.getSlot(31)?.stack ?: return
                chestType.value = 0
                chestType.price = Utils.getChestPrice(sssa.lore)
                if (Config.chestProfit) canOpen = true
                for (i in 9..17) {
                    val lootSlot = inv.getSlot(i)?.stack ?: continue
                    chestType.value += Utils.fetchEVERYWHERE(lootSlot.itemID) ?: Utils.fetchBzPrices(Utils.getBooksID(lootSlot)) ?: Utils.getEssenceValue(lootSlot.displayName)
                    if (Config.chestOpener && System.currentTimeMillis() - timeWait > 125 && notOpened) mc.thePlayer.closeScreen()
                }
            }
        }

    var canOpen = false
    var notOpened = true
    val noobmen = mutableListOf<Entity>()
    private var timeWait = 0L

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
        val items = mutableListOf<ItemStack>()

        fun reset() {
            price = 0
            value = 0
        }

        companion object {
            fun getFromName(name: String?): DungeonChest? {
                if (name.isNullOrBlank()) return null
                return entries.find {
                    it.displayText == name
                }
            }
        }
    }

    enum class Chestser(val s: String) {
        WOOD("Wooden Plank Chest"),
        GOLD("Golden Skull"),
        DIAMOND("Chest Diamond Right"),
        EMERALD("Chest Emerald Left"),
        OBSIDIAN("Chest Obsidian BottomLeft"),
        BEDROCK("Chest Bedrock (Front Bottom Right)")
    }
}
