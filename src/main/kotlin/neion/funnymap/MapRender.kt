package neion.funnymap

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.funnymap.map.MapUtils.mapRoomSize
import neion.ui.ScoreElement
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.RenderUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

object MapRender {

    private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
    private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
    private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
    private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
    private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
    private val defaultCross = ResourceLocation("funnymap", "default/cross.png")
    val lines
        get() = ScoreElement.runInformationLines()



    fun renderMap() {

        RenderUtil.renderRect(
            0.0,
            0.0,
            135.0,
            if (FMConfig.mapShowRunInformation == 1) 128 + lines.size * 2.5 else 128.0,
            FMConfig.mapBackground.toJavaColor()
        )

        RenderUtil.renderRectBorder(
            0.0,
            0.0,
            135.0,
            if (FMConfig.mapShowRunInformation == 1) 128 + lines.size * 2.5 else 128.0,
            FMConfig.mapBorderWidth.toDouble(),
            FMConfig.mapBorder.toJavaColor()
        )
        renderRooms()
        Dungeon.dungeonTeammates.forEach { (name, teammate) -> if (!teammate.dead) RenderUtil.drawPlayerHead(name, teammate) }
        if (FMConfig.mapShowRunInformation == 1) renderRunInformation()
    }

    private fun renderRooms() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = mapRoomSize shr 2

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.dungeonList[y * 11 + x] ?: continue

                val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                var color = tile.color

                    color = if (FMConfig.mapDarkenUndiscovered && RunInformation.started && tile.state == RoomState.UNDISCOVERED) color.run {
                        Color(
                            (red * (1 - FMConfig.mapDarkenPercent)).toInt(),
                            (green * (1 - FMConfig.mapDarkenPercent)).toInt(),
                            (blue * (1 - FMConfig.mapDarkenPercent)).toInt(), alpha)
                    } else color

                when {
                    xEven && yEven -> if (tile is Room) {
                        RenderUtil.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            mapRoomSize.toDouble(),
                            mapRoomSize.toDouble(),
                            color
                        )
                    }

                    !xEven && !yEven -> {
                        RenderUtil.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            color
                        )
                    }

                    else -> {
                        val doorwayOffset = if (mapRoomSize == 16) 5 else 6
                        val width = if (tile is Door) 6 else mapRoomSize
                        var x1 = if (!xEven) xOffset + mapRoomSize else xOffset
                        var y1 = if (!xEven) yOffset else yOffset + mapRoomSize
                        if (tile is Door) {
                            if (!xEven) y1 += doorwayOffset else x1 += doorwayOffset
                        }
                        RenderUtil.renderRect(
                            x1.toDouble(),
                            y1.toDouble(),
                            (if (!xEven) connectorSize else width).toDouble(),
                            (if (!xEven) width else connectorSize).toDouble(), color)
                    }
                }
            }
            val checkmarkSize = when (FMConfig.mapCheckmark) {
                1 -> 8.0 // default
                else -> 10.0 // neu
            }

            // Text / Checkmarks
            Dungeon.uniqueRooms.forEach { (room,pos) ->
                val xOffset = (pos.first shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (pos.second shr 1) * (mapRoomSize + connectorSize)

                if (FMConfig.mapCheckmark.equalsOneOf(0,1) && !(FMConfig.mapRoomNames == 1 && room.data.type == RoomType.PUZZLE)) {

                    when (FMConfig.mapCheckmark) {
                        0 -> when (room.state) {
                            RoomState.CLEARED -> defaultWhite
                            RoomState.GREEN -> defaultGreen
                            RoomState.FAILED -> defaultCross
                            else -> null
                        }

                        1 -> when (room.state) {
                            RoomState.CLEARED -> neuWhite
                            RoomState.GREEN -> neuGreen
                            RoomState.FAILED -> neuCross
                            else -> null
                        }

                        else -> null
                    }?.let {
                        GlStateManager.enableAlpha()
                        GlStateManager.color(255f, 255f, 255f, 255f)
                        mc.textureManager?.bindTexture(it)
                        RenderUtil.drawTexturedQuad(
                            xOffset + (mapRoomSize - checkmarkSize) / 2,
                            yOffset + (mapRoomSize - checkmarkSize) / 2,
                            checkmarkSize,
                            checkmarkSize
                        )
                        GlStateManager.disableAlpha()
                    }
                }

                val color = when (room.state) {
                    RoomState.GREEN -> Color.green
                    RoomState.CLEARED -> Color.white
                    RoomState.FAILED -> Color.red
                    RoomState.DISCOVERED -> Color.yellow
                    else -> Color.gray
                }

                if (FMConfig.mapCheckmark == 2) {
                    GlStateManager.pushMatrix()
                    GlStateManager.translate(
                        xOffset + (mapRoomSize shr 1).toFloat(),
                        yOffset + 2 + (mapRoomSize shr 1).toFloat(),
                        0f
                    )
                    GlStateManager.scale(2f, 2f, 1f)
                    RenderUtil.renderCenteredText(listOf(room.data.secrets.toString()), 0, 0, color)
                    GlStateManager.popMatrix()
                }

                val name = mutableListOf<String>()

                if (FMConfig.peekBind.isActive || FMConfig.mapCheckmark == 3 || (FMConfig.mapRoomNames != 0 && room.data.type.equalsOneOf(
                        RoomType.PUZZLE,
                        RoomType.TRAP) || FMConfig.mapRoomNames == 2 && room.data.type.equalsOneOf(
                        RoomType.NORMAL,
                        RoomType.RARE,
                        RoomType.CHAMPION))) name.addAll(room.data.name.split(" "))
                if (room.data.type == RoomType.NORMAL && FMConfig.mapRoomSecrets) name.add(room.data.secrets.toString())
                // Offset + half of roomsize
                RenderUtil.renderCenteredText(name, xOffset + mapRoomSize / 2, yOffset + mapRoomSize / 2, color)
            }
        }
        GlStateManager.popMatrix()
    }


    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(64f, 128f, 0f)
        GlStateManager.scale(2.0 / 3.0, 2.0 / 3.0, 1.0)
        val fr = mc.fontRendererObj
        val lineOne = lines.takeWhile { it != "split" }.joinToString(separator = "    ")
        val lineTwo = lines.takeWhile { it != "split1" }.takeLastWhile { it != "split" }.joinToString(separator = "    ")
        val lineThree = lines.takeLastWhile { it != "split" && it != "split1" }.joinToString(separator = "    ")
        val l1sw = -fr.getStringWidth(lineOne) / 2
        val l2Sw = -fr.getStringWidth(lineTwo) / 2
        RenderUtil.renderText(lineOne, l1sw, 0)
        RenderUtil.renderText(lineTwo, l2Sw, if (l1sw == 0) 0 else 9)
        RenderUtil.renderText(lineThree, -fr.getStringWidth(lineThree) / 2, if (l2Sw.and(l1sw) == 0) 0 else if (l2Sw.or(l1sw) == 0) 9 else 18)

        GlStateManager.popMatrix()
    }
}
