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
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object WeirdosSolver {

    private var riddleChest: BlockPos? = null

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


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Config.threeSolver || !inDungeons || event.type == 2.toByte()) return
        val formatted = event.message.formattedText ?: return
        if (formatted.startsWith("§a§lPUZZLE SOLVED ") && "wasn't fooled by " in formatted) riddleChest = null
        if (formatted.startsWith("§e[NPC] ")) {
            val chestLoc = EnumFacing.HORIZONTALS?.map { dir -> mc.theWorld?.loadedEntityList?.find { it is EntityArmorStand && formatted.substringAfter("§c").substringBefore("§f") in it.customNameTag }?.position?.offset(dir) }?.find { mc.theWorld?.getBlockState(it)?.block == Blocks.chest }
            if (formatted.containsAny(solutions)) riddleChest = chestLoc
            if (formatted.containsAny(wrong)) mc.theWorld.setBlockState(chestLoc, Blocks.air.defaultState)
        }
    }

    @SubscribeEvent
    fun onWorld(e: RenderWorldLastEvent) {
        if (!inDungeons || !Config.threeSolver) return
        if (riddleChest != null) RenderUtil.drawBlockBox(riddleChest!!, Color.GREEN, outline = false, fill = true, esp = false)
            if (Config.autoWeirdos) {
                mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.customNameTag.contains("CLICK") }?.forEach {
                    if (EditMode.getCurrentRoomPair()?.first?.data?.name == "Three Weirdos" && !inter.contains(it) && System.currentTimeMillis() - lastInter > 100 && mc.thePlayer.getDistanceToEntity(it) < 4) {
                        lastInter = System.currentTimeMillis()
                        inter.add(it)
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                    }
                }
            }
        }
    }