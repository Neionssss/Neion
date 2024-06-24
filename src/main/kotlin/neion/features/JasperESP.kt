package neion.features

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.NumberSetting
import neion.utils.RenderUtil
import neion.utils.Utils
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object JasperESP: Module("Jasper ESP", category = Category.GENERAL) {

    val range = NumberSetting("Scan Range", min = 250.0, max = 1750.0)

    init {
        addSettings(range)
    }

    var scanning = false
    var espModeMap: List<BlockPos>? = null

    // https://i.imgur.com/7LYblVE.png
    @SubscribeEvent
    fun scanBlocks(e: TickEvent.ClientTickEvent) {
        if (!Utils.getArea().contains("Crystal Hollows") || scanning) return
        val range = range.value
        scanning = true
        Thread {
            espModeMap = BlockPos.getAllInBox((mc.thePlayer.position.add(range, 150.0, range)),
                (mc.thePlayer.position.add(-range, -150.0, -range))).filter {
                mc.theWorld.getBlockState(it).block.equalsOneOf(Blocks.stained_glass_pane, Blocks.stained_glass) &&
                        (mc.theWorld.getBlockState(it).getValue(BlockStainedGlass.COLOR) == EnumDyeColor.MAGENTA ||
                                mc.theWorld.getBlockState(it).getValue(BlockStainedGlassPane.COLOR) == EnumDyeColor.MAGENTA) }
            mc.thePlayer.playSound("random.pop", 1f, 5f)
            scanning = false
        }.start()
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!Utils.getArea().contains("Crystal Hollows")) return
        if (espModeMap != null) espModeMap!!.stream().forEach { RenderUtil.drawBlockBox(it,Color.magenta, outline = true, fill = false, esp = true) }
    }
}