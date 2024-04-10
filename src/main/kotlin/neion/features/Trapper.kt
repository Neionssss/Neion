package neion.features

import neion.Config
import neion.Neion.Companion.mc
import neion.utils.RenderUtil
import neion.utils.TextUtils
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.equalsOneOf
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.passive.*
import net.minecraft.util.MovementInputFromOptions
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.util.*
import kotlin.concurrent.schedule


/* Credit AmbientAddons
https://github.com/appable0/AmbientAddons
*/

object Trapper {
    private var color: Color? = null
    private var trapperCooldown = 0L
    private var clicker = true

    @SubscribeEvent
    fun onChat(e: ClientChatReceivedEvent) {
        if (!Config.trapperESP) return

        val message = e.message ?: return
        val siblings = message.siblings ?: return
        if (siblings.getOrNull(0)?.unformattedText == "Accept the trapper's task to hunt the animal?") TextUtils.sendMessage(siblings.getOrNull(3)?.chatStyle?.chatClickEvent?.value ?: return)

        if (message.unformattedText.containsAny("Return to the Trapper soon to get a new animal to hunt!", "I couldn't locate any animals. Come back in a little bit!")) {
            Timer("Trapper").schedule(850) {
                color = null
                clicker = true
                TextUtils.sendCommand("warp trapper")
            }
            return
        }
        val colorMatch = Regex("^§e\\[NPC] Trevor§f: §rYou can find your §(?<color>[0-9a-f])§l\\w+ §fanimal near the §(?<locationColor>[0-9a-f])(?<location>[\\w ]+)§f.§r$").find(message.formattedText) ?: return
        color = Color(mc.fontRendererObj.getColorCode(colorMatch.groups["color"]!!.value[0]))
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (Config.trapperESP) mc.theWorld?.loadedEntityList?.forEach {
            if (mc.thePlayer.getDistanceToEntity(it) < 7 && clicker && it.displayName.unformattedText.contains("Trevor") && System.currentTimeMillis() - trapperCooldown > 25000) {
                mc.playerController.interactWithEntitySendPacket(mc.thePlayer, it)
                clicker = false
                trapperCooldown = System.currentTimeMillis()
            }
            if (color != null && it.ticksExisted >= 20 && it::class.equalsOneOf(
                    EntityCow::class,
                    EntityPig::class,
                    EntitySheep::class,
                    EntityChicken::class,
                    EntityRabbit::class,
                    EntityHorse::class) && (it as? EntityLiving)?.maxHealth.equalsOneOf(100F, 200F, 500F, 1000F, 1024F, 2048F)) {
                RenderUtil.renderBeaconBeam(it, color!!.rgb)
                RenderUtil.drawEntityBox(it, color!!, fill = true, esp = true)
            }
        }
    }
}