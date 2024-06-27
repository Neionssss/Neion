package neion.features.dungeons

import neion.events.CheckRenderEntityEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.ColorSetting
import neion.utils.Location.inBoss
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object ItemESP: Module("ItemESP", category = Category.DUNGEON) {

    val color = ColorSetting("Color", default = Color.yellow)

    val correctItems = listOf(
        Items.ender_pearl,
        Items.spawn_egg,
        Items.potionitem,
        Items.skull,
        Items.shears,
        ItemBlock.getItemFromBlock(Blocks.iron_trapdoor),
        ItemBlock.getItemFromBlock(Blocks.skull),
        ItemBlock.getItemFromBlock(Blocks.heavy_weighted_pressure_plate)
    )

    init {
        addSettings(color)
    }

    @SubscribeEvent
    fun onRenderWorld(e: CheckRenderEntityEvent) {
        if (!inDungeons || inBoss) return
        val entity = e.entity as? EntityItem ?: return
        if (mc.thePlayer.getDistanceToEntity(entity) < 10 && correctItems.any { it == entity.entityItem?.item }) RenderUtil.drawEntityBox(entity, color.value, outline = true, fill = false, esp = true)
    }
}