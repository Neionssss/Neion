/* Credit LiquidBounce Hacked Client
https://github.com/CCBlueX/LiquidBounce/
 */
package neion.features

import neion.Config
import neion.Neion.Companion.mc
import neion.events.PacketSentEvent
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.cos
import kotlin.math.sin

object FreeCam {
    private var fakePlayer: EntityOtherPlayerMP? = null

    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0

    @SubscribeEvent
    fun onEnable(e: ClientTickEvent) {
        val thePlayer = mc.thePlayer ?: return
        if (Config.freeCam) {
            oldX = thePlayer.posX
            oldY = thePlayer.posY
            oldZ = thePlayer.posZ
            val playerMP = EntityOtherPlayerMP(mc.theWorld, thePlayer.gameProfile)
            playerMP.rotationYawHead = thePlayer.rotationYawHead
            playerMP.renderYawOffset = thePlayer.renderYawOffset
            playerMP.rotationYawHead = thePlayer.rotationYawHead
            playerMP.copyLocationAndAnglesFrom(thePlayer)
            mc.theWorld!!.addEntityToWorld(-1000, playerMP)
            thePlayer.noClip = true
            fakePlayer = playerMP
            fakePlayer!!.isInvisible = true
        } else if (fakePlayer != null) {
            thePlayer.setPositionAndRotation(oldX, oldY, oldZ, thePlayer.rotationYaw, thePlayer.rotationPitch)
            mc.theWorld!!.removeEntityFromWorld(fakePlayer!!.entityId)
            fakePlayer = null
            thePlayer.motionX = 0.0
            thePlayer.motionY = 0.0
            thePlayer.motionZ = 0.0
        }
    }

    @SubscribeEvent
    fun onUpdate(e: LivingUpdateEvent?) {
        if (!Config.freeCam) return
        val thePlayer = mc.thePlayer ?: return
        thePlayer.noClip = true
        thePlayer.fallDistance = 0.0f
        thePlayer.motionY = 0.0
        thePlayer.motionX = 0.0
        thePlayer.motionZ = 0.0
        if (mc.gameSettings.keyBindJump.isKeyDown) thePlayer.motionY += Config.speedVaue
        if (mc.gameSettings.keyBindSneak.isKeyDown) thePlayer.motionY -= Config.speedVaue
        if (mc.thePlayer?.movementInput?.moveForward != 0f || mc.thePlayer?.movementInput?.moveStrafe != 0f) {
            mc.thePlayer.motionX = -sin(direction) * Config.speedVaue
            mc.thePlayer.motionZ = cos(direction) * Config.speedVaue
        }
    }

    @SubscribeEvent
    fun onPacket(e: PacketSentEvent) {
        if (!Config.freeCam) return
        val packet = e.packet
        if (packet is C03PacketPlayer || packet is C0BPacketEntityAction || packet is C08PacketPlayerBlockPlacement) e.isCanceled = true
    }

    val direction: Double
        get() {
            var rotationYaw = mc.thePlayer.rotationYaw
            if (mc.thePlayer.moveForward < 0f) rotationYaw += 180f
            var forward = 1f
            if (mc.thePlayer.moveForward < 0f) forward = -0.5f else if (mc.thePlayer.moveForward > 0f) forward = 0.5f
            if (mc.thePlayer.moveStrafing > 0f) rotationYaw -= 90f * forward
            if (mc.thePlayer.moveStrafing < 0f) rotationYaw += 90f * forward
            return Math.toRadians(rotationYaw.toDouble())
        }
}