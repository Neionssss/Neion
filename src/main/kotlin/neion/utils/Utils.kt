package neion.utils

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils
import neion.Neion.Companion.mc
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.stripControlCodes
import neion.utils.MathUtil.romanToDecimal
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange

object Utils {

	var lastClickTime = 0L

	val ItemStack.extraAttributes: NBTTagCompound?
		get() = this.getSubCompound("ExtraAttributes", false)

	val GuiScreen.chest: ContainerChest?
		get() = (this as? GuiChest)?.inventorySlots as? ContainerChest
	val ItemStack.itemID: String
		get() = this.extraAttributes?.getString("id") ?: ""

	val IInventory.items: List<ItemStack?>
		get() = (0 until this.sizeInventory).map { this.getStackInSlot(it) }

	fun inMurderMystery(): Boolean {
		if (mc.theWorld == null || mc.thePlayer == null || mc.isSingleplayer || !HypixelUtils.INSTANCE.isHypixel) return false
		return mc.theWorld.scoreboard.scores.any { it.objective.displayName.contains("murder", true) } && mc.thePlayer.inventory.getStackInSlot(4)?.item == Items.filled_map
	}

	fun fetchBzPrices(item: String): Int? {
		val fewprices = APIHandler.profitData?.getAsJsonObject("products")?.getAsJsonObject(item)
		val customItem = fewprices?.entrySet()?.map { it.key }?.get(3)
		val itemi = fewprices?.getAsJsonObject(customItem) ?: return null
		return itemi.get("sellPrice")?.asInt!!
	}


	fun getArea(): String {
		if (!inSkyblock) return ""
		for (entry in mc.netHandler?.playerInfoMap!!) {
			val areaText = entry?.displayName?.unformattedText ?: continue
			if (areaText.startsWith("Area: ")) return areaText.substringAfter("Area: ")
		}
		return ""
	}

	fun getBooksID(itemStack: ItemStack): String {
		val enchants = itemStack.extraAttributes?.getCompoundTag("enchantments")
		val enchant = enchants?.keySet?.firstOrNull()
		if (enchant != null) {
			return "ENCHANTMENT_${enchant.uppercase()}_${enchants.getInteger(enchant)}"
		}
		return ""
	}

	fun getIdFromName(name: String): String {
		return if (name.stripControlCodes().containsAny("Wither Shield", "Implosion", "Shadow Warp")) "${name}_SCROLL".replace(" ", "_").uppercase() else
		if (name.contains("Wither Cloak Sword", true)) "WITHER_CLOAK" else
			if (name.startsWith("§aEnchanted Book (")) {
				val enchantId = name.substringBeforeLast(" ").stripControlCodes().uppercase().replace(" ", "_")
				val level = name.substringAfterLast(" ").stripControlCodes().romanToDecimal()
				if (!name.stripControlCodes().containsAny("Wisdom",
						"Soul Eater",
						"Bank",
						"Legion",
						"No Pain No Gain",
						"One For All",
						"Last Stand")) "ENCHANTMENT_${enchantId}_$level" else "ENCHANTMENT_ULTIMATE_${enchantId}_$level"
			}
			else name.stripControlCodes().uppercase().replace(" ", "_").replace("`S", "").replace("'S", "")
	}

	fun getEssenceValue(text: String): Int {
		val groups = Regex("§d(?<type>\\w+) Essence §8x(?<count>\\d+)").matchEntire(text)?.groups ?: return 0
		val type = groups["type"]?.value?.uppercase() ?: return 0
		val count = groups["count"]?.value?.toInt() ?: return 0
		return (fetchBzPrices("ESSENCE_$type") ?: 0) * count
	}

	fun getChestPrice(lore: List<String>): Int {
		lore.forEach {
			val line = it.stripControlCodes()
			if (line.contains("FREE")) return 0
			if (line.contains(" Coins")) return line.substring(0, line.indexOf(" ")).replace(",", "").toInt()
		}
		return 0
	}


	// Credit Skyblock-Client
	private var windowClicks = 0
	private var startWindowID = 0
	fun clickSlot(slotNumber: Int) {
		var windowID = if (windowClicks == 0) mc.thePlayer.openContainer.windowId
		else startWindowID + windowClicks
		if (windowID > 100) windowID -= 100
		lastClickTime = System.currentTimeMillis()
		mc.playerController.windowClick(windowID, slotNumber, 2,3, mc.thePlayer)
	}

	fun fetchEVERYWHERE(name: String): Int? {
		return fetchBzPrices(name) ?: APIHandler.auctionData?.getAsJsonPrimitive(name)?.asInt
	}

	// wow FLC
	fun useItem(itemSlot: Int, swapBack: Boolean = true) {
		if (itemSlot < 9) {
			val previous = mc.thePlayer.inventory.currentItem

			mc.thePlayer.inventory.currentItem = itemSlot
			mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(itemSlot))
			mc.thePlayer.sendQueue.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(itemSlot)))
			if (swapBack) {
				mc.thePlayer.inventory.currentItem = previous
				mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(previous))
			}
		}
	}
}