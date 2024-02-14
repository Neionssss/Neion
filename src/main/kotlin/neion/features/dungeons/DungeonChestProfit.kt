package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.GuiContainerEvent
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.ItemUtils.lore
import neion.utils.Location.inBoss
import neion.utils.Utils
import neion.utils.Utils.itemID
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DungeonChestProfit {


    // https://i.imgur.com/k7mgy9U.png
    @SubscribeEvent
    fun onNo(e: GuiContainerEvent.DrawSlotEvent) {
        if (!Config.chestProfit || e.container !is ContainerChest || !inBoss) return
        val inv = e.container.lowerChestInventory
        val chestType = DungeonChest.getFromName(e.chestName) ?: return
        val openChest = inv.getStackInSlot(31) ?: return
        chestType.value = 0
        chestType.price = Utils.getChestPrice(openChest.lore)
        if (openChest.displayName == "§aOpen Reward Chest") {
            canOpen = true
            for (i in 9..17) {
                val lootSlot = inv.getStackInSlot(i) ?: continue
                if (lootSlot.lore.isNotEmpty()) {
                    val books = Utils.fetchEVERYWHERE(lootSlot.itemID) ?: Utils.fetchBzPrices(
                        Utils.enchantNameToID(
                            lootSlot.lore[0]
                        )
                    ) ?: Utils.getEssenceValue(lootSlot.displayName)
                    chestType.value += books
                }
            }
        }
    }

    var canOpen = false
    val noobmen = mutableListOf<Entity>()
    var timeWait = 0L

    // https://i.imgur.com/yRyrZc5.png
    @SubscribeEvent
    fun onChest(e: ClientTickEvent) {
        if (!Config.chestOpener || !inBoss) return
        mc.theWorld?.loadedEntityList?.filter {
            it is EntityArmorStand &&
                    it.getEquipmentInSlot(4)?.item.equalsOneOf(
                        ItemBlock.getItemFromBlock(Blocks.wooden_slab),
                        ItemBlock.getItemFromBlock(Blocks.stone_slab)
                    ) && mc.thePlayer.getDistanceToEntity(it) < 30
        }?.forEach {
            if (mc.thePlayer.openContainer as? ContainerChest == null && System.currentTimeMillis() - timeWait > 30 && !noobmen.contains(it)) {
                mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                noobmen.add(it)
                timeWait = System.currentTimeMillis()
                mc.thePlayer.closeScreen()
            }
        }
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

        companion object {
            fun getFromName(name: String?): DungeonChest? {
                if (name.isNullOrBlank()) return null
                return entries.find {
                    it.displayText == name
                }
            }
        }
    }
}
