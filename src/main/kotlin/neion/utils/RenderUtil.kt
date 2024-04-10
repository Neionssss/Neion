/* Most of the functions here are either Skyblock-Client or FunnyMap
https://github.com/Harry282
 */

package neion.utils

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.events.RenderLivingEntityEvent
import neion.funnymap.Dungeon
import neion.funnymap.map.DungeonPlayer
import neion.funnymap.map.MapUtils
import neion.mixins.MinecraftAccessor
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.itemID
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*


object RenderUtil {

    private val tessellator = Tessellator(2097152)
    private val worldRenderer = tessellator.worldRenderer
    private val fontRenderer = mc.fontRendererObj


    fun Color.bind() = color(red / 255f, green / 255f, blue / 255f, alpha / 255f)

    fun preDraw() {
        enableAlpha()
        enableBlend()
        disableDepth()
        disableLighting()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
    }

    fun postDraw() {
        disableAlpha()
        disableBlend()
        enableDepth()
        enableLighting()
        enableTexture2D()
    }

    fun Entity.getExtraInterPos() = Triple(
        lastTickPosX + (posX - lastTickPosX) * pticks() - mc.renderManager.viewerPosX,
        lastTickPosY + (posY - lastTickPosY) * pticks() - mc.renderManager.viewerPosY,
        lastTickPosZ + (posZ - lastTickPosZ) * pticks() - mc.renderManager.viewerPosZ
    )

    fun drawAABB(aabb: AxisAlignedBB, color: Color, fill: Boolean = false) {
        color.bind()

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

        if (fill) {
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
        }
        tessellator.draw()
    }


    infix fun Slot.highlight(color: Color) {
        disableLighting()
        Gui.drawRect(
            xDisplayPosition,
            yDisplayPosition,
            xDisplayPosition + 16,
            yDisplayPosition + 16,
            color.rgb
        )
    }


    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     * @author Mojang
     */
    fun renderBeaconBeam(entity: Entity, rgb: Int, alphaMultiplier: Float = 1.0f) {
        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        mc.textureManager.bindTexture(ResourceLocation("textures/entity/beacon_beam.png"))
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, 10497.0f)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, 10497.0f)
        disableLighting()
        enableCull()
        enableTexture2D()
        tryBlendFuncSeparate(770, 1, 1, 0)
        enableBlend()
        tryBlendFuncSeparate(770, 771, 1, 0)
        val time = mc.theWorld.totalWorldTime
        val d1 = floor(-time * 0.2 - floor(-time * 0.1))
        val r = (rgb shr 16 and 0xFF) / 255f
        val g = (rgb shr 8 and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val d2 = time * 0.025 * -1.5
        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
        val d6 = 0.5 + cos(d2 + PI / 4.0) * 0.2
        val d7 = 0.5 + sin(d2 + PI / 4.0) * 0.2
        val d14 = -1.0 + d1
        val d15 = height * 2.5 + d14
        val (x, y, z) = entity.getExtraInterPos()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
        disableCull()
        val d12 = -1.0 + d1
        val d13 = height + d12
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, fill: Boolean, esp: Boolean) {
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        if (esp) glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glDepthMask(false)
        glLineWidth(1.5f)
        drawAABB(
            mc.theWorld.getBlockState(blockPos)?.block?.getSelectedBoundingBox(mc.theWorld, blockPos)!!,
            color,
            fill
        )
        glDepthMask(true)
        glPopAttrib()
        glPopMatrix()
    }


    fun drawEntityBox(
        entity: Entity,
        color: Color,
        esp: Boolean,
        fill: Boolean = false,
        offset: Triple<Float, Float, Float> = Triple(0F, 0F, 0F),
        expansion: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0)
    ) {
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_TEXTURE_2D)
        if (esp) glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glDepthMask(false)
        glLineWidth(2.0f)
        drawAABB(entity.entityBoundingBox.run {
            AxisAlignedBB(
                minX - entity.posX,
                minY - entity.posY,
                minZ - entity.posZ,
                maxX - entity.posX,
                maxY - entity.posY,
                maxZ - entity.posZ
            ).offset(offset.first.toDouble(), offset.second.toDouble(), offset.third.toDouble())
                .expand(expansion.first, expansion.second, expansion.third)
        }, color, fill)
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
        val (x,y,z) = mc.renderViewEntity.getExtraInterPos()
        glPushMatrix()
        translate(x, y, z)
        enableBlend()
        enableAlpha()
        disableLighting()
        tryBlendFuncSeparate(770, 771, 1, 0)
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
        disableBlend()
        enableAlpha()
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

    fun drawPlayerHead(name: String, player: DungeonPlayer) {
        pushMatrix()

        // Translate to the player's location, updated every tick
        val xTranslate = if (player.isPlayer || name == mc.thePlayer.name) (mc.thePlayer.posX - Dungeon.STARTX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first else player.mapX.toDouble()
        val zTranslate = if (player.isPlayer || name == mc.thePlayer.name) (mc.thePlayer.posZ - Dungeon.STARTZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second else player.mapZ.toDouble()

        translate(xTranslate, zTranslate, 0.0)

        // Apply head rotation and scaling
        rotate(player.yaw + 180f, 0f, 0f, 1f)
        scale(MapConfig.playerHeadScale, MapConfig.playerHeadScale, 1f)

        // Draw the player head marker if required
        if (MapConfig.mapVanillaMarker && (player.isPlayer || name == mc.thePlayer.name)) {
            rotate(180f, 0f, 0f, 1f)
            color(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(ResourceLocation("funnymap", "marker.png"))
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
            worldRenderer.pos(-6.0, 6.0, 0.0).tex(0.0, 0.0).endVertex()
            worldRenderer.pos(6.0, 6.0, 0.0).tex(1.0, 0.0).endVertex()
            worldRenderer.pos(6.0, -6.0, 0.0).tex(1.0, 1.0).endVertex()
            worldRenderer.pos(-6.0, -6.0, 0.0).tex(0.0, 1.0).endVertex()
            tessellator.draw()
            rotate(-180f, 0f, 0f, 1f)
        } else {
            preDraw()
            enableTexture2D()
            color(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(player.skin)
            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)

            if (player.renderHat) Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
            postDraw()
        }

        // Handle player names
        if (name != mc.thePlayer.name && (MapConfig.peekBind.isActive || MapConfig.playerHeads == 2 ||
                    (MapConfig.playerHeads == 1 && mc.thePlayer.heldItem?.itemID.equalsOneOf(
                        "SPIRIT_LEAP",
                        "INFINITE_SPIRIT_LEAP",
                        "HAUNT_ABILITY")))) {
            rotate(-player.yaw + 180f, 0f, 0f, 1f)
            renderText(name, -fontRenderer.getStringWidth(name) / 2, 10, scale = MapConfig.playerNameScale.toDouble())
        }
        popMatrix()
    }

    fun renderText(
        text: String,
        x: Int,
        y: Int,
        scale: Double = 1.0,
        color: Int = 0xFFFFFF,
    ) {
        pushMatrix()
        disableLighting()
        disableBlend()
        scale(scale, scale, scale)
        var yOffset = y - fontRenderer.FONT_HEIGHT
        text.split("\n").forEach {
            yOffset += (fontRenderer.FONT_HEIGHT * scale).toInt()
            fontRenderer.drawString(
                it,
                round(x / scale).toFloat(),
                round(yOffset / scale).toFloat(),
                color,
                true
            )
        }
        popMatrix()
    }

    fun outlineESP(event: RenderLivingEntityEvent, color: Color) {
        fun render() = event.modelBase.render(
            event.entity,
            event.p_77036_2_,
            event.p_77036_3_,
            event.p_77036_4_,
            event.p_77036_5_,
            event.p_77036_6_,
            event.scaleFactor
        )
        // Cache the original settings
        val originalFancyGraphics = mc.gameSettings.fancyGraphics
        val originalGamma = mc.gameSettings.gammaSetting

        // Modify settings for the duration of the ESP rendering
        mc.gameSettings.fancyGraphics = false
        mc.gameSettings.gammaSetting = 100000f

        // Save the current OpenGL state and modify it for ESP rendering
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)

        // Bind the desired color and disable unnecessary features
        color.bind()
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)

        // Enable and configure necessary features for ESP rendering
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(2f)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)

        // Clear and configure the stencil buffer
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(GL_DONT_CARE)
        glStencilFunc(GL_NEVER, 1, GL_DONT_CARE)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)

        // Render the model in two passes using the stencil buffer to create an outline
        glPolygonMode(GL_BACK, GL_LINE)
        render()
        glStencilFunc(GL_NEVER, 0, GL_DONT_CARE)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_BACK, GL_FILL)
        render()
        glStencilFunc(GL_EQUAL, 1, GL_DONT_CARE)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_BACK, GL_LINE)
        render()

        // Offset and render the outline with depth testing disabled
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1.0f, -2000000f)
        render()

        // Restore the original OpenGL state
        glPopAttrib()
        glPopMatrix()

        // Restore the original settings
        mc.gameSettings.fancyGraphics = originalFancyGraphics
        mc.gameSettings.gammaSetting = originalGamma
    }

    fun drawOutlinedEntityItem(entityItem: Entity, color: Color) {
        val (x,y,z) = entityItem.getExtraInterPos()
        glPushMatrix()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(x,y,z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(x, y + entityItem.height, z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(x + entityItem.width, y + entityItem.height, z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(x + entityItem.width, y, z).color(color.red, color.green, color.blue, color.alpha).endVertex()
        tessellator.draw()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glPopMatrix()
    }


    fun pticks() = (mc as MinecraftAccessor).timer.renderPartialTicks

}