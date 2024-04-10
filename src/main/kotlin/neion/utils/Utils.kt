package neion.utils

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils
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
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

object Utils {

	var lastClickTime = 0L

	// Thanks UnBloomedClaim6
	fun fn(num: Int) = num.toString().replace("(\\d)(?=(\\d{3})+(?!\\d))".toRegex(), "$1,")

	fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

	val ItemStack.lore: List<String>
		get() {
			val display = getSubCompound("display", false)
			if (display != null && display.hasKey("Lore", 9)) {
				val nbt = display.getTagList("Lore", 8)
				val lore = ArrayList<String>()
				for (i in 0 until nbt.tagCount()) lore.add(nbt.getStringTagAt(i))
				return lore
			}
			return emptyList()
		}

	fun ItemStack.cleanName() = stripControlCodes(displayName)


	fun BlockPos.sendRightClick() {
		EnumFacing.HORIZONTALS.map {
			mc.playerController.onPlayerRightClick(
				mc.thePlayer,
				mc.theWorld,
				mc.thePlayer.heldItem,
				this,
				it,
				Vec3(x.toDouble(), y.toDouble(), z.toDouble()))
		}
	}

	val ItemStack.extraAttributes: NBTTagCompound?
		get() = getSubCompound("ExtraAttributes", false)

	val GuiScreen.chest: ContainerChest?
		get() = (this as? GuiChest)?.inventorySlots as? ContainerChest
	val ItemStack.itemID: String
		get() = extraAttributes?.getString("id") ?: ""

	val IInventory.items: List<ItemStack?>
		get() = (0 until sizeInventory).map { getStackInSlot(it) }

	fun inMurderMystery() = HypixelUtils.INSTANCE.isHypixel && mc.theWorld?.scoreboard?.scores?.any { it.objective.displayName.contains("murder", true) } == true && mc.thePlayer?.inventory?.getStackInSlot(4)?.item == Items.filled_map

	fun fetchBzPrices(item: String): Int? {
		val fewprices = APIHandler.profitData?.getAsJsonObject("products")?.getAsJsonObject(item)
		return fewprices?.getAsJsonObject(fewprices.entrySet()?.map { it.key }?.get(3))?.get("sellPrice")?.asInt
	}


	fun getArea(): String {
		if (!inSkyblock || mc.theWorld == null) return ""
		for (entry in mc.netHandler?.playerInfoMap!!) {
			val areaText = entry?.displayName?.unformattedText ?: continue
			if (areaText.startsWith("Area: ")) return areaText.substringAfter("Area: ")
		}
		return ""
	}

	fun getBooksID(itemStack: ItemStack): String {
		val enchants = itemStack.extraAttributes?.getCompoundTag("enchantments")
		val enchant = enchants?.keySet?.firstOrNull() ?: return ""
		return "ENCHANTMENT_${enchant.uppercase()}_${enchants.getInteger(enchant)}"
	}

	fun getEssenceValue(text: String): Int {
		val groups = Regex("§d(?<type>\\w+) Essence §8x(?<count>\\d+)").matchEntire(text)?.groups ?: return 0
		return (fetchBzPrices("ESSENCE_${groups["type"]?.value?.uppercase()}") ?: 0) * groups["count"]?.value?.toInt()!!
	}

	fun getChestPrice(lore: List<String>): Int {
		lore.forEach {
			val line = stripControlCodes(it)
			return if (line.contains(" Coins")) line.substring(0, line.indexOf(" ")).replace(",", "").toInt() else 0
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
		mc.playerController.windowClick(windowID, slotNumber, 2, 3, mc.thePlayer)
	}

	fun fetchEVERYWHERE(name: String) = fetchBzPrices(name) ?: APIHandler.auctionData?.getAsJsonPrimitive(name)?.asInt
}