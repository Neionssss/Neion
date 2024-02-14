package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ItemESP {

    private val validItems = listOf(
        Items.ender_pearl,
        Items.spawn_egg,
        Items.potionitem,
        Items.skull,
        Items.shears,
        ItemBlock.getItemFromBlock(Blocks.iron_trapdoor),
        ItemBlock.getItemFromBlock(Blocks.heavy_weighted_pressure_plate)
    )

    @SubscribeEvent
    fun onRenderWorld(e: CheckRenderEntityEvent<*>) {
        if (!Config.itemESP || !inDungeons) return
        val entity = e.entity as? EntityItem ?: return
        if (mc.thePlayer.getDistanceSqToEntity(entity) < 200 && entity.entityItem.item.equalsOneOf(validItems))
            RenderUtil.drawEntityBox(entity, Config.itemColor.toJavaColor(), outline = true, fill = true,esp = true)
    }
}

