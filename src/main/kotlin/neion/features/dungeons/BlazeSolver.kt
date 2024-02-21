/*
 https://github.com/UnclaimedBloom6/BloomModule || /ct import Bloom
 */
package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.funnymap.map.ScanUtils
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BlazeSolver {
    // https://i.imgur.com/eL8sXos.png. entityArmorStandMutableList will forever be a legend
    var blist = mutableListOf<Entity>()

    fun onTick() {
        if (!Config.blazeSolver || !inDungeons) return
        val hpMap = mutableMapOf<Entity, Int>()
        blist.clear()
        mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.name.contains("Blaze") && it.name.contains("/") }.forEach {
            val health = it.name.stripControlCodes().substringAfter("/").dropLast(1).replace(",", "").toInt()
            hpMap[it] = health
            blist.add(it)
            blist.sortWith { a, b -> hpMap[b]?.let { it1 -> hpMap[a]?.compareTo(it1) }!! }
            if (blist.isEmpty()) return
            val (x, z) = ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
            if (mc.theWorld.getBlockState(BlockPos(x + 1, 118, z)).block !== Blocks.cobblestone) blist.reverse()
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {
        if (!Config.blazeSolver) return
        blist.forEachIndexed { i, blaze ->
            val color = if (i == 0) Color.green else if (i == 1) Color.yellow else if (i == 2) Color.red else return
            RenderUtil.drawEntityBox(EntityBlaze(mc.theWorld), color, esp = false, fill = false, outline = true, offset = Triple(blaze.posX.toFloat(),(blaze.posY-2).toFloat(),blaze.posZ.toFloat()))
            if (Config.lineToNextBlaze && i > 0 && i < Config.blazeLines) {
                val pos1 = blist[i - 1]
                val pos = blist[i]
                RenderUtil.draw3DLine(
                    Vec3(pos1.posX, pos1.posY, pos1.posZ),
                    Vec3(pos.posX, pos.posY, pos.posZ),
                    4f,
                    color
                )
            }
        }
    }
    @SubscribeEvent
    fun onChec(e: CheckRenderEntityEvent<*>) {
        if (Config.blazeSolver && e.entity.name.stripControlCodes().startsWith("[Lv15] Blaze ")) e.isCanceled = true
    }
}