package neion.features

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Utils
import neion.utils.RenderUtil
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color


object JasperESP {

    var scanning = false
    var stopped = false
    var espModeMap = HashSet<BlockPos>()

    // https://i.imgur.com/7LYblVE.png
    @SubscribeEvent
    fun scanBlocks(e: ClientTickEvent) {
        if (!Utils.getArea().contains("Crystal Hollows") || !Config.JasperESP || scanning) return
        scanning = true
        Thread {
            BlockPos.getAllInBox(
                (mc.thePlayer.position.add(+Config.JasperESPRange, +100, +Config.JasperESPRange)),
                (mc.thePlayer.position.add(-Config.JasperESPRange, -150, -Config.JasperESPRange))
            ).filter { mc.theWorld.getBlockState(it).block.equalsOneOf(Blocks.stained_glass_pane, Blocks.stained_glass) && (mc.theWorld.getBlockState(it).getValue(BlockStainedGlass.COLOR) == EnumDyeColor.MAGENTA || mc.theWorld.getBlockState(it).getValue(BlockStainedGlassPane.COLOR) == EnumDyeColor.MAGENTA)
            }.forEach { espModeMap.add(it) }
            mc.thePlayer.playSound("random.pop", 1f, 5f)
            if (!stopped) scanning = false
        }.start()
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!Utils.getArea().contains("Crystal Hollows")) return
        if (Config.JasperESP) espModeMap.forEach { RenderUtil.drawBlockBox(it,Color.magenta, true, false, true) }
        if (Config.chestESP) (mc.theWorld?.loadedTileEntityList as? TileEntityChest)?.pos?.let {
            RenderUtil.drawBlockBox(it, Color.blue,outline = true, fill = false, esp = true)
        }
    }
}