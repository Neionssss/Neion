package neion.funnymap

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.map.DoorType
import neion.funnymap.map.RoomState
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.RenderUtil.getInterpolatedPosition
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object WitherDoorESP {
    @SubscribeEvent
    fun onRender(e: RenderWorldLastEvent) {
        if (!Location.inDungeons || FMConfig.witherDoorESP == 0 || Location.inBoss) return
        val (x, y, z) = mc.renderViewEntity.getInterpolatedPosition()
        Dungeon.doors.filter { it.type.equalsOneOf(DoorType.BLOOD,DoorType.WITHER) && !it.opened }.forEach {
            if (it.state == RoomState.UNDISCOVERED && FMConfig.witherDoorESP == 1) return@forEach
            val aabb = AxisAlignedBB(it.x - 1.0, 69.0, it.z - 1.0, it.x + 2.0, 73.0, it.z + 2.0)
            val color = if (it.type == DoorType.BLOOD && Dungeon.Info.bloodKey || it.type == DoorType.WITHER && Dungeon.Info.keys > 0) FMConfig.keyC else FMConfig.noKeyC
                GlStateManager.pushMatrix()
                RenderUtil.preDraw()
                GlStateManager.depthMask(true)
                GL11.glLineWidth(FMConfig.witherDoorOutlineWidth)
                RenderUtil.drawOutlinedAABB(aabb.offset(-x, -y, -z), color.toJavaColor())
                GlStateManager.depthMask(true)
                RenderUtil.postDraw()
                GlStateManager.popMatrix()
        }
    }
}
