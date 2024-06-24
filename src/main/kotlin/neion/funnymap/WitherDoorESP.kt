package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.map.Door
import neion.funnymap.map.DoorType
import neion.funnymap.map.RoomState
import neion.ui.Mapping
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.RenderUtil.getInterpolatedPosition
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object WitherDoorESP {
    @SubscribeEvent
    fun onRender(e: RenderWorldLastEvent) {
        if (!Location.inDungeons || Mapping.doorESP.selected == "OFF" || Location.inBoss) return
        val (x, y, z) = mc.renderViewEntity.getInterpolatedPosition()
        Dungeon.dungeonList.filterIsInstance<Door>().filter { it.type.equalsOneOf(DoorType.BLOOD,DoorType.WITHER) && !it.opened }.forEach {
            if (it.state == RoomState.UNDISCOVERED && Mapping.doorESP.selected == "First") return@forEach
            val aabb = AxisAlignedBB(it.x - 1.0, 69.0, it.z - 1.0, it.x + 2.0, 73.0, it.z + 2.0).offset(-x, -y, -z)
            val color = if (it.type == DoorType.BLOOD && RunInformation.bloodKey || it.type == DoorType.WITHER && RunInformation.keys > 0) Mapping.keyColor.value else Mapping.noKeyColor.value
            GlStateManager.pushMatrix()
            RenderUtil.preDraw()
            GL11.glLineWidth(Mapping.doorOutlineWidth.value.toFloat())
            RenderUtil.drawOutlinedAABB(aabb, color)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
        }
    }
}
