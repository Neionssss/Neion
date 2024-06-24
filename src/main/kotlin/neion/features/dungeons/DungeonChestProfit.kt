package neion.features.dungeons

import neion.events.GuiContainerEvent
import neion.funnymap.ScoreCalculation.score
import neion.ui.HudElement
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.Location
import neion.utils.Location.inBoss
import neion.utils.RenderUtil
import neion.utils.Utils
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.itemID
import neion.utils.Utils.lore
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object DungeonChestProfit: Module("Dungeon Chest Profit", category = Category.DUNGEON) {

    val looter = BooleanSetting("Chest Looter")
    val openBest = BooleanSetting("Open Best Chest")
    val buyChest = BooleanSetting("Buy Best Chest")
    val openSecond = BooleanSetting("Open Second-Profit")
    val buySecond = BooleanSetting("Buy Second-Profit")
    val profitX = NumberSetting("ProfitX", default = 50.0, hidden = true)
    val profitY = NumberSetting("ProfitY", default = 50.0, hidden = true)
    val profitScale = NumberSetting("ProfitScale", default = 1.0, min = 0.1, max = 4.0, hidden = true)
    var nextAble = true
    var timeWait = 0L

    val worthless = listOf(
        "DUNGEON_DISC_5",
        "MAXOR_THE_FISH",
        "STORM_THE_FISH",
        "GOLDOR_THE_FISH",
    )

    init {
        addSettings(looter, openBest, buyChest, openSecond, buySecond, profitScale, profitX, profitY)
    }

    var notOpened = true

    fun getChestsSize(): Int {
        return when (score) {
            in 300..317 -> if (Location.dungeonFloor.equalsOneOf(5,6,7)) 6 else 5
            in 270..299 -> 5
            in 230..269 -> 4
            in 160..229 -> 3
            else -> 2
        }
    }


    // https://i.imgur.com/k7mgy9U.png
    @SubscribeEvent
    fun scanChests(e: ClientTickEvent) {
        if (!inBoss || !looter.enabled) return
        DungeonChest.entries.forEach { chest ->
            if (nextAble && !chest.canDraw) {
                val entity = getEntityFromChest(chest) ?: return@forEach
                nextAble = false
                mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT))
            }
        }
        if (openBest.enabled && notOpened && DungeonChest.entries.count { it.canDraw } == getChestsSize()) {
            openBestChest()
            openSecondChest()
        }
    }

    @SubscribeEvent
    fun countValue(e: GuiContainerEvent) {
        if (!inBoss || e.container !is ContainerChest) return
        val chestType = DungeonChest.entries.find { e.chestName == it.displayText } ?: return
        val sssa = e.container.inventorySlots?.get(31)?.stack ?: return
        chestType.value = 0
        chestType.price = Utils.getChestPrice(sssa.lore)
        chestType.canDraw = true
        for (i in 9..17) {
            val lootSlot = e.container.inventorySlots?.get(i)?.stack ?: continue
            if (lootSlot.itemID in worthless || lootSlot.item == Item.getItemFromBlock(Blocks.stained_glass_pane)) continue
            chestType.items.add(lootSlot.itemID)
            chestType.value += Utils.fetchEVERYWHERE(lootSlot.itemID) ?: Utils.fetchBzPrices(Utils.getBooksID(lootSlot)) ?: Utils.getEssenceValue(lootSlot.displayName)
        }
        if (looter.enabled) {
            if (notOpened) {
                mc.thePlayer.closeScreen()
                nextAble = true
            } else if (openBest.enabled && buyChest.enabled) {
                val secondChest = DungeonChest.entries.sortedBy { it.profit }.reversed()[1]
                if (e.chestName == DungeonChest.entries.maxByOrNull { it.profit }?.displayText) {
                    Utils.clickSlot(31)
                }
                if (openSecond.enabled && buySecond.enabled && e.chestName == secondChest.displayText && secondChest.profit > Utils.fetchEVERYWHERE("DUNGEON_CHEST_KEY")?.coerceAtLeast(100000)!!) {
                    Utils.clickSlot(31)
                }
            }
        }
    }



    fun openBestChest() {
        notOpened = false
        timeWait = System.currentTimeMillis()
        val bestChest = if (DungeonChest.entries.any { it.items.equalsOneOf(alwaysBuy) }) DungeonChest.entries.find { it.items.equalsOneOf(alwaysBuy) } else DungeonChest.entries.maxByOrNull { it.profit }
        mc.netHandler.addToSendQueue(C02PacketUseEntity(bestChest?.let { getEntityFromChest(it) }, C02PacketUseEntity.Action.INTERACT))
    }

    fun openSecondChest() {
        if (!openSecond.enabled || DungeonChest.entries.sortedBy { it.profit }.reversed()[1].profit < Utils.fetchEVERYWHERE("DUNGEON_CHEST_KEY")?.coerceAtLeast(100000)!! || timeWait == 0L || System.currentTimeMillis() - timeWait < 100) return
        val secondChest = DungeonChest.entries.sortedBy { it.profit }.reversed()[1]
        mc.netHandler.addToSendQueue(C02PacketUseEntity(getEntityFromChest(secondChest), C02PacketUseEntity.Action.INTERACT))
    }


    fun getEntityFromChest(chest: DungeonChest): EntityArmorStand? {
        return mc.theWorld?.loadedEntityList?.filter { mc.thePlayer.getDistanceToEntity(it) < 20 }?.filterIsInstance<EntityArmorStand>()?.find { it.getEquipmentInSlot(4)?.cleanName() == chest.eqName }
    }

    val alwaysBuy = listOf(
    "NECRON_HANDLE",
    "SHADOW_WARP_SCROLL",
    "IMPLOSION_SCROLL",
    "WITHER_SHIELD_SCROLL",
    )

    // ----------------------------------------------------

    enum class DungeonChest(var displayText: String, var eqName: String) {
        WOOD("Wood Chest", "Wooden Plank Chest"),
        GOLD("Gold Chest", "Golden Skull"),
        DIAMOND("Diamond Chest", "Chest Diamond Left"),
        EMERALD("Emerald Chest", "Chest Emerald Right"),
        OBSIDIAN("Obsidian Chest", "Chest Obsidian BottomLeft"),
        BEDROCK("Bedrock Chest", "Chest Bedrock (Front Bottom Right)");

        var price = 0
        var value = 0
        var items: ArrayList<String> = arrayListOf()
        var canDraw = false
        val profit
            get() = value - price

        fun reset() {
            price = 0
            value = 0
            canDraw = false
            items.clear()
        }
    }



    object ChestProfitElement : HudElement(profitX, profitY,87,70,profitScale) {

        override fun shouldRender() = inBoss

        override fun render() {
            DungeonChest.entries.sortedBy { it.profit }.reversed().forEachIndexed { i, chest -> if (chest.canDraw) RenderUtil.renderText(chest.displayText + ": " + "§f§"  + (if (chest.profit > 0) "a" else "c") + Utils.fn(chest.profit), 0, 0 + i * 11) }
        }
    }
}
