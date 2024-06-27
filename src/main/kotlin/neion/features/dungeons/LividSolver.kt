package neion.features.dungeons

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.Location
import neion.utils.RenderUtil
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object LividSolver: Module("Livid Solver", category = Category.DUNGEON) {

    var rightLivid: Livid? = null

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (!Location.inBoss || Location.dungeonFloor != 5) return
        val block = mc.theWorld?.getBlockState(BlockPos(5,108,43)) ?: return
        rightLivid = Livid.entries.find { it.woolMetadata == block.block.getMetaFromState(block) }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (rightLivid == null || !Location.inBoss || Location.dungeonFloor != 5) return
        val lividToHighlight = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.find { it.displayName.formattedText.contains(rightLivid!!.chatColor) && it.displayName.unformattedText.contains("Livid") } ?: return
        RenderUtil.drawBlockBox(BlockPos(lividToHighlight.posX,lividToHighlight.posY - 1, lividToHighlight.posZ), rightLivid!!.color, esp = true, fill = false, outline = true)
    }


    enum class Livid(var woolMetadata: Int, var chatColor: String, var color: Color) {
        VENDETTA(0, "§f", Color.white),
        CROSSED(2, "§d", Color.pink),
        ARCADE(4, "§e", Color.yellow),
        SMILE(5, "§a", Color.green),
        DOCTOR(7, "§7", Color.gray),
        PURPLE(10, "§5", Color(160,0,250)),
        SCREAM(11, "§9", Color.black),
        FROG(13, "§2", Color(0,100,0)),
        HOCKEY(14, "§c", Color.red);
    }
}