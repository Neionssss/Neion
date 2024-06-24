package neion.utils

import neion.Neion.Companion.mc
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants

object Utils {

	var lastClickTime = 0L

	fun fn(num: Int) = num.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")

	val ItemStack.extraAttributes: NBTTagCompound?
		get() = getSubCompound("ExtraAttributes", false)

	val ItemStack.itemID: String
		get() = extraAttributes?.getString("id") ?: ""

	val IInventory.items: List<ItemStack?>
		get() = (0 until sizeInventory).map { getStackInSlot(it) }


	fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

	fun Any?.equalsOneOf(other: List<Any>): Boolean = other.any { this == it }

	val ItemStack.lore: List<String>
		get() {
			val display = this.getSubCompound("display", false) ?: return emptyList()
			if (display.hasKey("Lore", 9)) {
				val nbt = display.getTagList("Lore", 8)
				val lore = ArrayList<String>()
				for (ii in 0 until nbt.tagCount()) lore.add(nbt.getStringTagAt(ii))
				return lore
			}
			return emptyList()
		}

	fun ItemStack.cleanName() = displayName.stripControlCodes()

	fun inMurderMystery() = mc.theWorld != null && mc.thePlayer != null && Location.isHypixel() && mc.theWorld.scoreboard.scores.any { it.objective.displayName.contains("murder", true) } && mc.thePlayer.inventory.getStackInSlot(4)?.item == Items.filled_map

	fun getArea(): String {
		if (!inSkyblock) return ""
		for (entry in mc.netHandler?.playerInfoMap!!) {
			val areaText = entry?.displayName?.unformattedText ?: continue
			if (areaText.startsWith("Area: ")) return areaText.substringAfter("Area: ")
		}
		return ""
	}

	fun fetchBzPrices(item: String): Int? {
		val fewprices = APIHandler.profitData?.getAsJsonObject("products")?.getAsJsonObject(item)
		val customItem = fewprices?.entrySet()?.map { it.key }?.get(3)
		val itemi = fewprices?.getAsJsonObject(customItem) ?: return null
		return itemi.get("sellPrice")?.asInt ?: 0
	}

	fun getBooksID(itemStack: ItemStack): String {
		val enchants = itemStack.extraAttributes?.getCompoundTag("enchantments")
		val enchant = enchants?.keySet?.firstOrNull() ?: return ""
		return "ENCHANTMENT_${enchant.uppercase()}_${enchants.getInteger(enchant)}"
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
		if (System.currentTimeMillis() - lastClickTime < 100) return
		var windowID = if (windowClicks == 0) mc.thePlayer?.openContainer?.windowId else startWindowID + windowClicks
		if (windowID != null) {
			if (windowID > 100) windowID -= 100
			lastClickTime = System.currentTimeMillis()
			mc.playerController.windowClick(windowID, slotNumber, 2, 3, mc.thePlayer)
		}
	}

	fun ItemStack.getSkullTexture(): String? {
		if (item != Items.skull) return null
		if (tagCompound == null) return null
		val nbt = tagCompound
		if (!nbt.hasKey("SkullOwner")) return null
		return nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
	}

	fun fetchEVERYWHERE(name: String) = fetchBzPrices(name) ?: APIHandler.auctionData?.getAsJsonPrimitive(name)?.asInt
}