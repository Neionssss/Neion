/* Credit LiquidBounce Hacked Client
https://github.com/CCBlueX/LiquidBounce
 */
package neion.features

import cc.polyfrost.oneconfig.renderer.font.Fonts
import neion.Config
import neion.Neion.Companion.mc
import neion.events.Render3DEvent
import neion.events.RenderLivingEntityEvent
import neion.utils.RenderUtil
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Team
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object PlayerESP {

    @SubscribeEvent
    fun fucknedbr(e: RenderLivingEntityEvent) {
        if (!Config.playerESP) return
        val entity = e.entity as? EntityOtherPlayerMP ?: return
        if ((entity.team as? ScorePlayerTeam)?.nameTagVisibility == Team.EnumVisible.NEVER || entity.name == mc.thePlayer.name) return
        RenderUtil.outlineESP(e, Color.red)
    }


    @SubscribeEvent
    fun onRender3D(e: Render3DEvent) {
        if (!Config.playerESP) return
        mc.theWorld?.playerEntities?.forEach {
            if ((it?.team as? ScorePlayerTeam)?.nameTagVisibility == Team.EnumVisible.NEVER || ((Config.freeCam || Config.showOwnName) && it.name == mc.thePlayer.name)) return
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glPushMatrix()
            // Disable lightning and depth test
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            // Enable blend
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            renderNameTag(it, it.name)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
            // Reset color
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
    }

    // ---------------------------------------------------------------------
    // https://i.imgur.com/MNAHAmQ.png
    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        val fontRenderer = mc.fontRendererObj
        // Modify tag
        val nameColor = if (entity.isInvisible) "§6" else if (entity.isSneaking) "§4" else "§7"
        val healthText = "§7§c " + entity.health.toInt() + " HP"
        val text = "$nameColor$tag$healthText"
        // Push
        GL11.glPushMatrix()
        // Translate to player position
        val renderManager = mc.renderManager
        GL11.glTranslated( // Translate to player position with render pos and interpolate it
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * RenderUtil.pticks() - renderManager.viewerPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * RenderUtil.pticks() - renderManager.viewerPosY + entity.eyeHeight.toDouble() + 0.55,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * RenderUtil.pticks() - renderManager.viewerPosZ
        )
        GL11.glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        GL11.glRotatef(renderManager.playerViewX, 1F, 0F, 0F)

        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) * 0.2f
        if (distance < 1F) distance = 1F
        val scale = distance / 100f
        GL11.glScalef(-scale, -scale, scale)
        // Draw NameTag
        val width = fontRenderer.getStringWidth(text) * 0.5f
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2d(width + 4F.toDouble(), (-2F).toDouble())
        GL11.glVertex2d(-width - 2F.toDouble(), (-2F).toDouble())
        GL11.glVertex2d(-width - 2F.toDouble(), fontRenderer.FONT_HEIGHT + 2F.toDouble())
        GL11.glVertex2d(width + 4F.toDouble(), fontRenderer.FONT_HEIGHT + 2F.toDouble())
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)

        fontRenderer.drawString(text, 1F + -width, if (fontRenderer == Fonts.MINECRAFT_REGULAR) 1F else 1.5F, Color.white.rgb, true)
            mc.renderItem.zLevel = -147F
            for (index in (0..4)) mc.renderItem.renderItemAndEffectIntoGUI(entity.getEquipmentInSlot(index), -50 + index * 20, -22)
            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.enableTexture2D()
        // Pop
        GL11.glPopMatrix()
    }
}