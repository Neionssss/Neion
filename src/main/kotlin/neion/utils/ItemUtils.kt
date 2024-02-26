package neion.utils

import neion.utils.TextUtils.stripControlCodes
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.Constants

object ItemUtils {

    fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

    fun ItemStack.getSkullTextured(): String? {
        if (item != Items.skull) return null
        if (tagCompound == null) return null
        val nbt = tagCompound
        if (!nbt.hasKey("SkullOwner")) return null
        return nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties")
            .getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
    }

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

    fun ItemStack.cleanName() = this.displayName.stripControlCodes()
}