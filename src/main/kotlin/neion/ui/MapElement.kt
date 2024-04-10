package neion.ui

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.funnymap.map.*
import neion.funnymap.map.MapUtils.mapRoomSize
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

object MapElement : MovableGuiElement("Map") {

    private val lines = ScoreElement().lines

    override val h: Int
        get() = if (MapConfig.mapShowRunInformation == 1) (128 + lines.size * 2.5).toInt() else 128
    override val w: Int = 135
    override fun render() {
        RenderUtil.renderRect(
            0.0,
            0.0,
            135.0,
            if (MapConfig.mapShowRunInformation == 1) 128 + lines.size * 2.5 else 128.0,
            MapConfig.mapBackground.toJavaColor())

        RenderUtil.renderRectBorder(
            0.0,
            0.0,
            135.0,
            if (MapConfig.mapShowRunInformation == 1) 128 + lines.size * 2.5 else 128.0,
            MapConfig.mapBorderWidth.toDouble(),
            MapConfig.mapBorder.toJavaColor())


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

                color = if (MapConfig.mapDarkenUndiscovered && RunInformation.startTime != 0L && tile.state == RoomState.UNDISCOVERED) color.run {
                    Color(
                        (red * (1 - MapConfig.mapDarkenPercent)).toInt(),
                        (green * (1 - MapConfig.mapDarkenPercent)).toInt(),
                        (blue * (1 - MapConfig.mapDarkenPercent)).toInt(), alpha)
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
            val checkmarkSize = when (MapConfig.mapCheckmark) {
                1 -> 8.0 // default
                else -> 10.0 // neu
            }

            // Text / Checkmarks
            Dungeon.uniqueRooms.forEach { (room,pos) ->
                val xOffset = (pos.first shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (pos.second shr 1) * (mapRoomSize + connectorSize)

                if (MapConfig.mapCheckmark.equalsOneOf(0,1) && !(MapConfig.mapRoomNames == 1 && room.data.type == RoomType.PUZZLE)) {

                    when (MapConfig.mapCheckmark) {
                        0 -> when (room.state) {
                            RoomState.CLEARED -> ResourceLocation("funnymap", "default/white_check.png")
                            RoomState.GREEN -> ResourceLocation("funnymap", "default/green_check.png")
                            RoomState.FAILED -> ResourceLocation("funnymap", "default/cross.png")
                            else -> null
                        }

                        1 -> when (room.state) {
                            RoomState.CLEARED -> ResourceLocation("funnymap", "neu/white_check.png")
                            RoomState.GREEN -> ResourceLocation("funnymap", "neu/green_check.png")
                            RoomState.FAILED -> ResourceLocation("funnymap", "neu/cross.png")
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

                if (MapConfig.mapCheckmark == 2) RenderUtil.renderText(room.data.secrets.toString(), xOffset + (mapRoomSize shr 1), yOffset + 2 + (mapRoomSize shr 1), color = color.rgb, scale = MapConfig.textScale.toDouble())

                val name = mutableListOf<String>()

                if (MapConfig.peekBind.isActive || MapConfig.mapCheckmark == 3 || (MapConfig.mapRoomNames != 0 && room.data.type.equalsOneOf(RoomType.PUZZLE, RoomType.TRAP) || MapConfig.mapRoomNames == 2 && room.data.type.equalsOneOf(RoomType.NORMAL, RoomType.RARE, RoomType.CHAMPION))) name.addAll(room.data.name.split(" "))
                if (room.data.type == RoomType.NORMAL && MapConfig.mapRoomSecrets) name.add(room.data.secrets.toString())
                name.forEachIndexed { index, texte -> RenderUtil.renderText(texte, x + mc.fontRendererObj.getStringWidth(texte) / -2, y + name.size * 12 / -2 + index * 12, color = color.rgb, scale = MapConfig.textScale.toDouble()) }
            }
        }
        GlStateManager.popMatrix()
        Dungeon.players.forEach { (name, teammate) -> if (!teammate.dead) RenderUtil.drawPlayerHead(name, teammate) }
        if (MapConfig.mapShowRunInformation == 1) {
            val lineOne = lines.takeWhile { it != "split" }.joinToString(separator = "    ")
            val lineTwo = lines.takeWhile { it != "split1" }.takeLastWhile { it != "split" }.joinToString(separator = "    ")
            val lineThree = lines.takeLastWhile { it != "split" && it != "split1" }.joinToString(separator = "    ")
            val l1sw = -mc.fontRendererObj.getStringWidth(lineOne) / 2
            val l2Sw = -mc.fontRendererObj.getStringWidth(lineTwo) / 2
            RenderUtil.renderText(lineOne, l1sw, 0)
            RenderUtil.renderText(lineTwo, l2Sw, if (l1sw == 0) 0 else 9)
            RenderUtil.renderText(lineThree, 64 -mc.fontRendererObj.getStringWidth(lineThree) / 2, 128 + if (l2Sw.and(l1sw) == 0) 0 else if (l2Sw.or(l1sw) == 0) 9 else 18, scale = 2.0/3.0)
        }
    }

    override fun shouldRender() = MapConfig.mapEnabled && Location.inDungeons || MapConfig.mapHideInBoss && !Location.inBoss
}
