/*
 https://github.com/UnclaimedBloom6/BloomModule || /ct import Bloom
 */
package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.funnymap.map.MapUtils.getRoomCentre
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BlazeSolver {
    private var blist = mutableListOf<Entity>()

    fun onTick() {
        if (!Config.blazeSolver || !inDungeons) return
        val hpMap = mutableMapOf<Entity, Int>()
        if (blist.isNotEmpty()) blist.clear()
        mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.name.contains("Blaze") && it.name.contains("/") }.forEach {
            hpMap[it] = stripControlCodes(it.name).substringAfter("/").dropLast(1).replace(",", "").toInt()
            blist.add(it)
            blist.sortBy { a -> hpMap[a] }
            if (blist.isEmpty()) return
            val (x, z) = getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
            if (mc.theWorld.getBlockState(BlockPos(x + 1, 118, z)).block != Blocks.cobblestone) blist.reverse()
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {
        blist.forEachIndexed { i, blaze ->
            val color = if (i == 0) Color.green else if (i == 1) Color.yellow else return
            RenderUtil.drawEntityBox(blaze, color, esp = false, offset = Triple(blaze.posX.toFloat(),(blaze.posY-2).toFloat(),blaze.posZ.toFloat()))
            if (Config.lineToNextBlaze && i > 0) {
                val pos1 = blist[0]
                val pos = blist[1]
                RenderUtil.draw3DLine(Vec3(pos1.posX, pos1.posY, pos1.posZ), Vec3(pos.posX, pos.posY, pos.posZ), 4f, color)
            }
        }
    }
    @SubscribeEvent
    fun onCheck(e: CheckRenderEntityEvent) {
        e.isCanceled = Config.blazeSolver && inDungeons && stripControlCodes(e.entity.name).startsWith("[Lv15] Blaze ")
    }
}