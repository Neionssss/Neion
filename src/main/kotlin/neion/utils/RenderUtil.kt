/* Most of the functions here are either Skyblock-Client or Skytils
https://github.com/Harry282/Skyblock-Client
https://github.com/Skytils/SkytilsMod
 */

package neion.utils

import neion.Config
import neion.FMConfig
import neion.Neion.Companion.mc
import neion.events.RenderLivingEntityEvent
import neion.funnymap.Dungeon
import neion.funnymap.map.DungeonPlayer
import neion.funnymap.map.MapUtils
import neion.mixins.MinecraftAccessor
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Utils.itemID
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import net.minecraft.util.*
import net.minecraftforge.client.MinecraftForgeClient
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import java.awt.Color
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin


object RenderUtil {

    private val tessellator = Tessellator.getInstance()
    private val worldRenderer = tessellator.worldRenderer


    fun Color.bind() = GlStateManager.color(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)

    fun preDraw() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    }

    fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
    }


    fun Entity.getInterpolatedPosition(): Triple<Double, Double, Double> {
        return Triple(
                this.lastTickPosX + (this.posX - this.lastTickPosX) * pticks(),
                this.lastTickPosY + (this.posY - this.lastTickPosY) * pticks(),
                this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * pticks()
        )
    }

    fun Entity.getExtraInterPos(): Triple<Double,Double,Double> {
        return Triple(
                this.lastTickPosX + (this.posX - this.lastTickPosX) * pticks() - mc.renderManager.viewerPosX,
                this.lastTickPosY + (this.posY - this.lastTickPosY) * pticks() - mc.renderManager.viewerPosY,
                this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * pticks() - mc.renderManager.viewerPosZ
        )
    }

    fun drawFilledAABB(aabb: AxisAlignedBB, color: Color) {
        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()

        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
    }



    fun drawOutlinedAABB(aabb: AxisAlignedBB, color: Color) {
        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)

        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()

        tessellator.draw()
    }

    infix fun Slot.highlight(color: Color) {
        GlStateManager.disableLighting()
        Gui.drawRect(
            this.xDisplayPosition,
            this.yDisplayPosition,
            this.xDisplayPosition + 16,
            this.yDisplayPosition + 16,
            color.rgb
        )
    }


    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     * @author Mojang
     */
    fun renderBeaconBeam(entity: Entity, rgb: Int, alphaMultiplier: Float) {
        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        mc.textureManager.bindTexture(ResourceLocation("textures/entity/beacon_beam.png"))
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, 10497.0f)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, 10497.0f)
        GlStateManager.disableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val time = mc.theWorld.totalWorldTime + pticks()
        val d1 = MathHelper.func_181162_h(
            -time * 0.2 - MathHelper.floor_double(-time * 0.1)
                .toDouble()
        )
        val r = (rgb shr 16 and 0xFF) / 255f
        val g = (rgb shr 8 and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val d2 = time * 0.025 * -1.5
        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
        val d6 = 0.5 + cos(d2 + Math.PI / 4.0) * 0.2
        val d7 = 0.5 + sin(d2 + Math.PI / 4.0) * 0.2
        val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
        val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
        val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
        val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
        val d14 = -1.0 + d1
        val d15 = height.toDouble() * 2.5 + d14
        val (x,y,z) = entity.getExtraInterPos()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
        GlStateManager.disableCull()
        val d12 = -1.0 + d1
        val d13 = height + d12
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean, fill: Boolean, esp: Boolean) {
        if (!outline && !fill) return
        val renderManager = mc.renderManager
        val x = blockPos.x - renderManager.viewerPosX
        val y = blockPos.y - renderManager.viewerPosY
        val z = blockPos.z - renderManager.viewerPosZ

        var axisAlignedBB = AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0)
        val block = mc.theWorld.getBlockState(blockPos).block
        if (block != null) {
            val player = mc.thePlayer
            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * pticks()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * pticks()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pticks()
            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos).offset(-posX, -posY, -posZ)
        }

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        if (esp) glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glDepthMask(false)

        if (outline) {
            glLineWidth(1.5f)
            drawOutlinedAABB(axisAlignedBB, color)
        }
        if (fill) drawFilledAABB(axisAlignedBB, color)
        glDepthMask(true)
        glPopAttrib()
        glPopMatrix()
    }


    fun drawEntityBox(
        entity: Entity,
        color: Color,
        outline: Boolean,
        fill: Boolean,
        esp: Boolean,
        offset: Triple<Float, Float, Float> = Triple(0F, 0F, 0F),
        expansion: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0)) {
        if (!outline && !fill) return
        val (x,y,z) = entity.getExtraInterPos()

        var axisAlignedBB: AxisAlignedBB
        entity.entityBoundingBox.run {
            axisAlignedBB = AxisAlignedBB(
                minX - entity.posX,
                minY - entity.posY,
                minZ - entity.posZ,
                maxX - entity.posX,
                maxY - entity.posY,
                maxZ - entity.posZ
            ).offset(x + offset.first, y + offset.second, z + offset.third).expand(expansion.first, expansion.second, expansion.third)
        }

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_TEXTURE_2D)
        if (esp) glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glDepthMask(false)

        if (outline) {
            glLineWidth(2.0f)
            drawOutlinedAABB(axisAlignedBB, color)
        }
        if (fill) drawFilledAABB(axisAlignedBB, color)
        if (esp) glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glPopAttrib()
        glPopMatrix()
    }

    fun draw3DLine(
        pos1: Vec3,
        pos2: Vec3,
        width: Float,
        color: Color,
    ) {
        val (x,y,z) = mc.renderViewEntity.getInterpolatedPosition()
        glPushMatrix()
        GlStateManager.translate(-x, -y, -z)
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        glLineWidth(width)
        glColor4f(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            (color.alpha * 1f / 255f).coerceAtMost(1f)
        )
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(pos1.xCoord, pos1.yCoord, pos1.zCoord).endVertex()
        worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex()
        tessellator.draw()
        glPopMatrix()
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        glColor4f(1f, 1f, 1f, 1f)
    }


    fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bind()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()
        postDraw()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bind()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)
        tessellator.draw()

        postDraw()
    }


    fun drawTexturedQuad(x: Double, y: Double, width: Double, height: Double) {
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }


    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
        if (text.isEmpty()) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(FMConfig.textScale, FMConfig.textScale, 1f)
        val fontHeight = mc.fontRendererObj.FONT_HEIGHT + 1
        val yTextOffset = text.size * fontHeight / -2

        text.forEachIndexed { index, texte ->
            renderText(texte, mc.fontRendererObj.getStringWidth(texte) / -2, yTextOffset + index * fontHeight, color = color)
        }
        GlStateManager.popMatrix()
    }

    fun drawPlayerHead(name: String, player: DungeonPlayer) {
        GlStateManager.pushMatrix()
        try {
            // Translates to the player's location which is updated every tick.
            if (player.isPlayer || name == mc.thePlayer.name) {
                GlStateManager.translate(
                    (mc.thePlayer.posX - Dungeon.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first,
                    (mc.thePlayer.posZ - Dungeon.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second,
                    0.0
                )
            } else GlStateManager.translate(player.mapX.toFloat(), player.mapZ.toFloat(), 0f)

            // Apply head rotation and scaling
            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            GlStateManager.scale(FMConfig.playerHeadScale, FMConfig.playerHeadScale, 1f)

            if (FMConfig.mapVanillaMarker && (player.isPlayer || name == mc.thePlayer.name)) {
                GlStateManager.rotate(180f, 0f, 0f, 1f)
                GlStateManager.color(1f, 1f, 1f, 1f)
                mc.textureManager.bindTexture(ResourceLocation("funnymap", "marker.png"))
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
                worldRenderer.pos(-6.0, 6.0, 0.0).tex(0.0, 0.0).endVertex()
                worldRenderer.pos(6.0, 6.0, 0.0).tex(1.0, 0.0).endVertex()
                worldRenderer.pos(6.0, -6.0, 0.0).tex(1.0, 1.0).endVertex()
                worldRenderer.pos(-6.0, -6.0, 0.0).tex(0.0, 1.0).endVertex()
                tessellator.draw()
                GlStateManager.rotate(-180f, 0f, 0f, 1f)
            } else {
                // Render black border around the player head
                renderRectBorder(-6.0, -6.0, 12.0, 12.0, 1.0, Color(0, 0, 0, 255))

                preDraw()
                GlStateManager.enableTexture2D()
                GlStateManager.color(1f, 1f, 1f, 1f)

                mc.textureManager.bindTexture(player.skin)

                Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
                if (player.renderHat) Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
                postDraw()
            }

            // Handle player names
            if (name != mc.thePlayer.name && (FMConfig.peekBind.isActive || FMConfig.playerHeads == 2 || FMConfig.playerHeads == 1 && mc.thePlayer.heldItem?.itemID.equalsOneOf("SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP", "HAUNT_ABILITY"))) {
                GlStateManager.rotate(-player.yaw + 180f, 0f, 0f, 1f)
                GlStateManager.translate(0f, 10f, 0f)
                GlStateManager.scale(FMConfig.playerNameScale, FMConfig.playerNameScale, 1f)
                renderText(name, -mc.fontRendererObj.getStringWidth(name) / 2, 0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    fun renderText(
        text: String,
        x: Int,
        y: Int,
        scale: Double = 1.0,
        color: Int = 0xFFFFFF,
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.scale(scale, scale, scale)
        var yOffset = y - mc.fontRendererObj.FONT_HEIGHT
        text.split("\n").forEach {
            yOffset += (mc.fontRendererObj.FONT_HEIGHT * scale).toInt()
            mc.fontRendererObj.drawString(
                it,
                round(x / scale).toFloat(),
                round(yOffset / scale).toFloat(),
                color,
                true
            )
        }
        GlStateManager.popMatrix()
    }

    fun outlineESP(event: RenderLivingEntityEvent, color: Color) {
        fun render() = event.modelBase.render(event.entity, event.p_77036_2_, event.p_77036_3_, event.p_77036_4_, event.p_77036_5_, event.p_77036_6_, event.scaleFactor)
        val fancyGraphics = mc.gameSettings.fancyGraphics
        val gamma = mc.gameSettings.gammaSetting
        mc.gameSettings.fancyGraphics = false
        mc.gameSettings.gammaSetting = 100000f
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        checkSetupFBO()
        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        renderOne()
        render()
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        render()
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        render()
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1.0f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
        render()
        glPopAttrib()
        glPopMatrix()
        mc.gameSettings.fancyGraphics = fancyGraphics
        mc.gameSettings.gammaSetting = gamma
    }

    private fun renderOne() {
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(Config.espOutlineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    private fun checkSetupFBO() {
        val fbo = mc.framebuffer ?: return
            if (fbo.depthBuffer > -1) {
                    EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
                    val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
                    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
                    EXTFramebufferObject.glRenderbufferStorageEXT(
                        EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                        EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
                        mc.displayWidth,
                        mc.displayHeight
                    )
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(
                        EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                        EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
                        EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                        stencilDepthBufferID
                    )
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(
                        EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                        EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
                        EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                        stencilDepthBufferID
                    )
                    if (fbo.depthBuffer > -1) fbo.depthBuffer = -1
                fbo.depthBuffer = -1
            }
        }

    fun pticks() = (mc as MinecraftAccessor).timer.renderPartialTicks
}