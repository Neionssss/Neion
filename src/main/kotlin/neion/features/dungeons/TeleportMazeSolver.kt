package neion.features.dungeons

import neion.events.PacketReceiveEvent
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.Location
import neion.utils.RenderUtil
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object TeleportMazeSolver: Module("TP Maze Solver", category = Category.DUNGEON) {

    val map: ConcurrentHashMap<BlockPos, Color> = ConcurrentHashMap()
    var rightOne: BlockPos? = null

    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        if (!Location.inDungeons || EditMode.getCurrentRoomPair()?.first?.data?.name != "TP Maze") return
        (e.packet as? S08PacketPlayerPosLook)?.apply {
            val tpPad = getPad(BlockPos(x, y, z)) ?: return
            getPad(mc.thePlayer.position)?.let { map.putIfAbsent(tpPad, Color.red) }
            if (tpPad !in map.keys) {
                map.putIfAbsent(tpPad, Color.red)
                val radians = PI / 180
                val yaw1 = (-yaw * radians - PI).toFloat()
                val pitch1 = -cos(-pitch * radians)
                val vec = Vec3(sin(yaw1) * pitch1, 69.0, cos(yaw1) * pitch1)
                for (i in 4..23) rightOne = getBlocks(BlockPos(x + vec.xCoord * i, vec.yCoord, z + vec.zCoord * i), 2).find { it !in map.keys && mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!Location.inDungeons || EditMode.getCurrentRoomPair()?.first?.data?.name != "TP Maze") return
        map.map { RenderUtil.drawBlockBox(it.key,it.value, outline = true, fill = true,esp = false) }
        rightOne?.let { RenderUtil.drawBlockBox(it, Color.green, outline = true, fill = true, esp = false) }
    }

    private fun getBlocks(center: BlockPos, radius: Int) = BlockPos.getAllInBox(BlockPos(center.x - radius, 69, center.z - radius), BlockPos(center.x + radius, 69, center.z + radius))
    private fun getPad(pos: BlockPos) = getBlocks(pos, 1).find { mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame }
}