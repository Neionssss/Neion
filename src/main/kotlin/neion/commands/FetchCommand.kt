package neion.commands

import neion.Neion.Companion.mc
import neion.utils.APIHandler
import neion.utils.ItemUtils.lore
import neion.utils.TextUtils
import neion.utils.Utils
import neion.utils.Utils.itemID
import neion.utils.MathUtil
import net.minecraft.client.entity.EntityPlayerSP

// DA
object FetchCommand : BaseCommand("fetch", listOf("fauc", "fbz")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        val heldItemId = mc.thePlayer.heldItem
        val fetchingers = Utils.fetchEVERYWHERE(heldItemId.itemID) ?: Utils.fetchEVERYWHERE(Utils.enchantNameToID(heldItemId.lore[0])) ?: return
        if (args.isEmpty()) {
            if (!mc.thePlayer.heldItem.isStackable) TextUtils.info("Current Lowest price of ${heldItemId.itemID} is ${MathUtil.fn(fetchingers)}")
            else TextUtils.info("Current price of ${heldItemId.stackSize} ${heldItemId.itemID} is ${MathUtil.fn(fetchingers * heldItemId.stackSize)} / Price per unit is ${MathUtil.fn(fetchingers)}")
        } else {
            if (args[0] == "refreshPrices") APIHandler.refreshData() else {
                val arg = Utils.getIdFromName(args[0])
                val fetchinger = Utils.fetchEVERYWHERE(arg) ?: return
                if (args.size > 1) {
                    val seconds = args[1].toInt()
                    TextUtils.info("The price of $seconds ${args[0]} is ${MathUtil.fn(fetchinger * seconds)} ")
                } else TextUtils.info("The price of ${args[0]} is ${MathUtil.fn(fetchinger)}")
            }
        }
    }
}