package neion.features.dungeons.terminals

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.RenderUtil
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object SimonSaysSolver {
    val clickInOrder = ArrayList<BlockPos>()
    var cleared = false

    @SubscribeEvent
    fun tickOck(e: ClientTickEvent) {
        if (!Config.ssSolver || !Location.inBoss) return
        for (y in 120..123) {
            for (z in 92..95) {
                val pos = BlockPos(111, y, z)
                if (mc.theWorld.getBlockState(pos).block == Blocks.sea_lantern && !clickInOrder.contains(pos)) clickInOrder.add(pos)
                else if (mc.theWorld.getBlockState(BlockPos(110,y,z)).block == Blocks.air) clickInOrder.clear()
            }
        }
    }

    @SubscribeEvent
    fun onInter(e: PlayerInteractEvent) {
        if (!e.action.equalsOneOf(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) || clickInOrder.isEmpty() || !Config.ssSolver) return
        val pos = e.pos
        val x = pos.x
        val y = pos.y
        val z = pos.z
        if (x == 110 && y == 121 && z == 91) clickInOrder.clear()
        if (mc.theWorld.getBlockState(BlockPos(x,y,z)).block != Blocks.stone_button) return
        val pose = BlockPos(x+1,y,z)
        if (clickInOrder.contains(pose)) clickInOrder.remove(pose) else if (!mc.thePlayer.isSneaking) e.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!Config.ssSolver) return
        clickInOrder.forEachIndexed { i, click ->
            val color = if (i == 0) Color.green else if (i == 1) Color.yellow else Color.red
            val x = click.x - mc.renderManager.viewerPosX
            val y = click.y - mc.renderManager.viewerPosY + .372
            val z = click.z - mc.renderManager.viewerPosZ + .308
            RenderUtil.drawFilledAABB(AxisAlignedBB(x,y,z, x - .13, y + .26, z + .382), color)
        }
    }
}