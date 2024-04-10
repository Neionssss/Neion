package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.funnymap.map.MapUtils.getCurrentRoom
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.sendRightClick
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object WeirdosSolver {

    var riddleChest: BlockPos? = null
    private var lastInter = 0L
    var notYet = true


    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Config.threeSolver || !inDungeons || event.type == 2.toByte() || getCurrentRoom()?.data?.name != "Three Weirdos") return
        val formatted = event.message.formattedText ?: return
        if (formatted.startsWith("§e[NPC] ") && formatted.containsAny(
                    "The reward is not in my chest!",
                    "At least one of them is lying, and the reward is not in",
                    "My chest doesn't have the reward. We are all telling the truth",
                    "My chest has the reward and I'm telling the truth!",
                    "The reward isn't in any of our chests",
                    "Both of them are telling the truth. Also")) riddleChest = EnumFacing.HORIZONTALS?.map { dir -> (mc.theWorld?.loadedEntityList?.find { it is EntityArmorStand && formatted.substringAfter("§c").substringBefore("§f") in it.customNameTag } ?: return).position?.offset(dir) }?.find { mc.theWorld?.getBlockState(it)?.block == Blocks.chest } ?: return
        }

    @SubscribeEvent
    fun onWorld(e: RenderWorldLastEvent) {
        if (!inDungeons || !Config.threeSolver || getCurrentRoom()?.data?.name != "Three Weirdos") return
        riddleChest?.let { chest ->
            RenderUtil.drawBlockBox(chest, Color.GREEN, fill = true, esp = false)
            if (Config.autoWeirdos) {
                mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.customNameTag.contains("CLICK") }?.forEach {
                        if (System.currentTimeMillis() - lastInter > 50 && mc.thePlayer.getDistanceToEntity(it) < 5) {
                            lastInter = System.currentTimeMillis()
                            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                            mc.theWorld.removeEntity(it)
                        }
                    }
                if (notYet) {
                    chest.sendRightClick()
                    notYet = false
                }
            }
        }
    }
}