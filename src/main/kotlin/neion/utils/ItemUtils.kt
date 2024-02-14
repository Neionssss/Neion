package neion.utils

import neion.Neion.Companion.mc
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraftforge.common.util.Constants

object ItemUtils {

    fun Any?.equalsOneOf(vararg other: Any): Boolean {
        return other.any { this == it }
    }

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
                for (ii in 0 until nbt.tagCount()) {
                    lore.add(nbt.getStringTagAt(ii))
                }
                return lore
            }
            return emptyList()
        }

    fun ItemStack.cleanName() = this.displayName.stripControlCodes()

    fun getTextureFromSkull(position: BlockPos?): String? {
        return (mc.theWorld.getTileEntity(position) as TileEntitySkull).serializeNBT().getCompoundTag("Owner").getCompoundTag("Properties").getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
    }
}