/*
 * Credit LiquidBounce Hacked Client
 * https://github.com/CCBlueX/LiquidBounce/
 */
package neion.utils

import neion.Neion.Companion.mc
import kotlin.math.cos
import kotlin.math.sin

object MovementUtils {

    val isMoving: Boolean
        get() = mc.thePlayer?.movementInput?.moveForward != 0f || mc.thePlayer?.movementInput?.moveStrafe != 0f

    fun strafe(speed: Float) {
        if (!isMoving) return
        mc.thePlayer.motionX = -sin(direction) * speed
        mc.thePlayer.motionZ = cos(direction) * speed
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