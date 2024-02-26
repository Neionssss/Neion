package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.Location.inDungeons
import neion.utils.RenderUtil
import neion.utils.TextUtils.containsAny
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object WeirdosSolver {

    var riddleChest: BlockPos? = null

    // Thanks Moolb https://i.imgur.com/ohLScw5.png
    private var solutions = listOf(
        "The reward is not in my chest!",
        "At least one of them is lying, and the reward is not in",
        "My chest doesn't have the reward. We are all telling the truth",
        "My chest has the reward and I'm telling the truth!",
        "The reward isn't in any of our chests",
        "Both of them are telling the truth. Also",
    )

    private var wrong = listOf(
        "One of us is telling the truth!",
        "They are both telling the truth.",
        "The reward isn't in.",
        "We are all telling the truth!",
        "is telling the truth and the reward is in his chest.",
        "My chest doesn't have the reward. At least one of the others is telling the truth!",
        "One of the others is lying!",
        "They are both telling the truth, the reward is in",
        "They are both lying, the reward is in my chest!",
        "The reward is in my chest!",
        "The reward is not in my chest. They are both lying.",
        "is telling the truth.",
        "My chest has the reward!"
    )
    val inter = HashSet<Entity>()
    var lastInter = 0L
    var notYet = true


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Config.threeSolver || !inDungeons || event.type == 2.toByte() || EditMode.getCurrentRoomPair()?.first?.data?.name != "Three Weirdos") return
        val formatted = event.message.formattedText ?: return
        if (formatted.startsWith("§e[NPC] ")) {
            val loadedEntities = mc.theWorld.loadedEntityList ?: return
            val entity = loadedEntities.find { it is EntityArmorStand && formatted.substringAfter("§c").substringBefore("§f") in it.customNameTag } ?: return
            val chestLoc = EnumFacing.HORIZONTALS?.map { dir -> entity.position?.offset(dir) }?.find { mc.theWorld?.getBlockState(it)?.block == Blocks.chest } ?: return
            if (formatted.containsAny(solutions)) riddleChest = chestLoc
            if (formatted.containsAny(wrong)) mc.theWorld.setBlockState(chestLoc, Blocks.air.defaultState)
            loadedEntities.filter { it.posX == entity.posX && it.posZ == entity.posZ }.forEach { mc.theWorld.removeEntity(it) }
        }
    }

    @SubscribeEvent
    fun onWorld(e: RenderWorldLastEvent) {
        if (!inDungeons || !Config.threeSolver || EditMode.getCurrentRoomPair()?.first?.data?.name != "Three Weirdos") return
        riddleChest?.let {
            RenderUtil.drawBlockBox(it, Color.GREEN, outline = false, fill = true, esp = false)
            EnumFacing.HORIZONTALS.map { dir ->
                if (Config.autoWeirdos && notYet) {
                    mc.playerController.onPlayerRightClick(mc.thePlayer,mc.theWorld,mc.thePlayer.heldItem, riddleChest, dir, Vec3(riddleChest!!.x.toDouble(), riddleChest!!.y.toDouble(), riddleChest!!.z.toDouble()))
                    notYet = false
                }}
        }
            if (Config.autoWeirdos) {
                mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.customNameTag.contains("CLICK") }?.forEach {
                    if (EditMode.getCurrentRoomPair()?.first?.data?.name == "Three Weirdos" && !inter.contains(it) && System.currentTimeMillis() - lastInter > 50 && mc.thePlayer.getDistanceToEntity(it) < 5) {
                        lastInter = System.currentTimeMillis()
                        inter.add(it)
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                    }
                }
            }
        }
    }