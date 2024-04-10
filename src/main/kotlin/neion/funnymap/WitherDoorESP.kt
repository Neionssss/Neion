package neion.funnymap

import neion.MapConfig
import neion.funnymap.map.Door
import neion.funnymap.map.DoorType
import neion.funnymap.map.RoomState
import neion.utils.Location
import neion.utils.RenderUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object WitherDoorESP {
    @SubscribeEvent
    fun onRender(e: RenderWorldLastEvent) {
        if (!Location.inDungeons || MapConfig.witherDoorESP == 0 || Location.inBoss) return
        Dungeon.dungeonList.filterIsInstance<Door>().filter { it.type != DoorType.NORMAL && !it.opened }.forEach {
            if (it.state == RoomState.UNDISCOVERED && MapConfig.witherDoorESP == 1) return@forEach
            GlStateManager.pushMatrix()
            RenderUtil.preDraw()
            GL11.glLineWidth(MapConfig.witherDoorOutlineWidth)
            RenderUtil.drawAABB(AxisAlignedBB(it.x - 1.0, 69.0, it.z - 1.0, it.x + 2.0, 73.0, it.z + 2.0), (if (it.type == DoorType.BLOOD && RunInformation.bloodKey || it.type == DoorType.WITHER && RunInformation.keys > 0) MapConfig.keyC else MapConfig.noKeyC).toJavaColor())
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
        }
    }
}
