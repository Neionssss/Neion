/* Credit LiquidBounce Hacked Client
https://github.com/CCBlueX/LiquidBounce/
 */
package neion.features

import neion.events.PacketSentEvent
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.NumberSetting
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object FreeCam: Module("FreeCam", hasBind = true) {

    val speedValue = NumberSetting("Speed", default = 2.0, min = 0.5, max = 5.0)

    init {
        addSettings(speedValue)
    }

    private var fakePlayer: EntityOtherPlayerMP? = null

    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0
    private var oldYaw = 0f
    private var oldPitch = 0f

    override fun onEnable() {
        if (mc.thePlayer == null) return
        super.onEnable()
        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        oldYaw = mc.thePlayer.rotationYawHead
        oldPitch = mc.thePlayer.rotationPitch
        val playerMP = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        playerMP.rotationYawHead = mc.thePlayer.rotationYawHead
        playerMP.renderYawOffset = mc.thePlayer.renderYawOffset
        playerMP.rotationYawHead = mc.thePlayer.rotationYawHead
        playerMP.copyLocationAndAnglesFrom(mc.thePlayer)
        mc.theWorld.addEntityToWorld(-1000, playerMP)
        mc.thePlayer.noClip = true
        fakePlayer = playerMP
        fakePlayer!!.isInvisible = true
    }

    override fun onDisable() {
        if (fakePlayer == null) return
        mc.thePlayer.setPositionAndRotation(oldX, oldY, oldZ, oldYaw, oldPitch)
        mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
        fakePlayer = null
        mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
        super.onDisable()
    }

    @SubscribeEvent
    fun onUpdate(e: LivingUpdateEvent?) {
        mc.thePlayer.noClip = true
        mc.thePlayer.fallDistance = 0.0f
        mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
        if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += speedValue.value
        if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= speedValue.value
        if (mc.thePlayer.movementInput?.moveForward == 0f && mc.thePlayer.movementInput?.moveStrafe == 0f) return
        mc.thePlayer.motionX = -sin(direction) * speedValue.value
        mc.thePlayer.motionZ = cos(direction) * speedValue.value
    }

    @SubscribeEvent
    fun onPacket(e: PacketSentEvent) {
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