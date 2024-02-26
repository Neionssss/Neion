package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.PacketSentEvent
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.RenderUtil
import neion.utils.Utils.extraAttributes
import neion.utils.Utils.itemID
import neion.utils.EtherwarpUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EtherwarpOverlay {

    fun isHoldingEtherwarpItem(): Boolean {
        val held = mc.thePlayer?.heldItem ?: return false
        if (!held.itemID.equalsOneOf("ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID")) return false
        return held.extraAttributes?.hasKey("ethermerge")!!
    }

    @SubscribeEvent
    fun onPacket(e: PacketSentEvent) = (e.packet as? C03PacketPlayer)?.let { EtherwarpUtils.sss(it) }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (Config.etherwarpOverlay && mc.thePlayer.isSneaking && isHoldingEtherwarpItem()) {
            val (x, y, z) = EtherwarpUtils.traverseVoxels() ?: return
            RenderUtil.drawBlockBox(BlockPos(x, y, z), Config.etherwarpColor.toJavaColor(), true, false, true)
        }
    }
}