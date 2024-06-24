/*
 https://github.com/UnclaimedBloom6/BloomModule || /ct import Bloom
 */
package neion.features.dungeons

import neion.events.CheckRenderEntityEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.BooleanSetting
import neion.utils.Location.inDungeons
import neion.utils.MapUtils
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
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object BlazeSolver: Module("Blaze Solver", category = Category.DUNGEON) {
    var blist = mutableListOf<Entity>()

    private val lineToNext = BooleanSetting("Line to next blaze")

    init {
        addSettings(lineToNext)
    }

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (!inDungeons) return
        val hpMap = mutableMapOf<Entity, Int>()
        blist.clear()
        mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.name.contains("Blaze") && it.name.contains("/") }.forEach {
            val health = it.name.stripControlCodes().substringAfter("/").dropLast(1).replace(",", "").toInt()
            hpMap[it] = health
            blist.add(it)
            blist.sortWith { a, b -> hpMap[b]?.let { it1 -> hpMap[a]?.compareTo(it1) }!! }
            if (blist.isEmpty()) return
            val (x, z) = MapUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
            if (mc.theWorld.getBlockState(BlockPos(x + 1, 118, z)).block != Blocks.cobblestone) blist.reverse()
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {
        blist.forEachIndexed { i, blaze ->
            val color = if (i == 0) Color.green else if (i == 1) Color.yellow else return
            RenderUtil.drawEntityBox(EntityBlaze(mc.theWorld), color, esp = false, fill = false, outline = true, offset = Triple(blaze.posX.toFloat(),(blaze.posY-2).toFloat(),blaze.posZ.toFloat()))
            if (lineToNext.enabled && i > 0) {
                val pos1 = blist[0]
                val pos = blist[1]
                RenderUtil.draw3DLine(
                    Vec3(pos1.posX, pos1.posY, pos1.posZ),
                    Vec3(pos.posX, pos.posY, pos.posZ),
                    4f,
                    color)
            }
        }
    }
    @SubscribeEvent
    fun onChec(e: CheckRenderEntityEvent) {
        if (e.entity.name.stripControlCodes().startsWith("[Lv15] Blaze ")) e.isCanceled = true
    }
}