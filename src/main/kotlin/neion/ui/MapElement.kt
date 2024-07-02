package neion.ui

import neion.features.RandomStuff
import neion.funnymap.Dungeon
import neion.funnymap.map.*
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.elements.advanced.AdvancedMenu
import neion.ui.clickgui.settings.BooleanSetting
import neion.ui.clickgui.settings.ColorSetting
import neion.ui.clickgui.settings.NumberSetting
import neion.ui.clickgui.settings.SelectorSetting
import neion.utils.Location
import neion.utils.Location.inDungeons
import neion.utils.MapUtils
import neion.utils.MapUtils.mapRoomSize
import neion.utils.RenderUtil
import neion.utils.RenderUtil.postDraw
import neion.utils.RenderUtil.preDraw
import neion.utils.RenderUtil.renderText
import neion.utils.RenderUtil.tessellator
import neion.utils.RenderUtil.worldRenderer
import neion.utils.Utils.equalsOneOf
import neion.utils.Utils.itemID
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import java.awt.Color

object Colors: Module("Colors", category = Category.MAP) {

    val mapBorder = ColorSetting("Map Border", default = Color(0, 0, 0, 255))
    val mapBackground = ColorSetting("Map Background", default = Color(0, 0, 0, 179/255))
    val normalRoom = ColorSetting("Normal Room", default = Color(107, 58, 17))
    val mimicRoom = ColorSetting("Mimic Room", default = Color(186, 66, 52))
    val trapRoom = ColorSetting("Trap Room", default = Color(216, 127, 51))
    val puzzleRoom = ColorSetting("Puzzle Room", default = Color(117, 0, 133))
    val entranceRoom = ColorSetting("Entrance Room", default = Color(20, 133, 0))
    val fairyRoom = ColorSetting("Fairy Room", default = Color(224, 0, 255))
    val miniBossRoom = ColorSetting("Miniboss Room", default = Color(254, 223, 0))
    val bloodRoom = ColorSetting("Blood Room", default = Color(231, 0, 0))
    val rareRoom = ColorSetting("Rare Room", default = Color(255, 203, 89))
    val normalDoor = ColorSetting("Normal Door", default = Color(92, 52, 14))
    val entranceDoor = ColorSetting("Entrance Door", default = Color(20, 133, 0))
    val witherDoor = ColorSetting("Wither Door", default = Color(0,0,0))
    val openedDoor = ColorSetting("Opened Door", default = Color(92, 52, 14))
    val bloodDoor = ColorSetting("Blood Door", default = Color(231, 0, 0))

    init {
        addSettings(mapBorder,
            mapBackground,
            normalRoom,
            mimicRoom,
            trapRoom,
            puzzleRoom,
            entranceRoom,
            fairyRoom,
            miniBossRoom,
            bloodRoom,
            rareRoom,
            normalDoor,
            entranceDoor,
            witherDoor,
            openedDoor,
            bloodDoor)
    }


    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun keyBind() {
        onEnable()
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        AdvancedMenu.openColors = true
    }

    override fun onDisable() {}
}


object Mapping: Module("Map", category = Category.MAP) {

    val mapEnabled = BooleanSetting("Map Enabled", enabled = true)
    val hideInBoss = BooleanSetting("Hide in Boss")
    val scanMimic = BooleanSetting("Scan Mimic", enabled = true)
    val highlightMimic = BooleanSetting("Highlight Mimic", description = "Draws a box at mimic chest when you're in the same room")
    val darkenUndiscovered = BooleanSetting("Darken Rooms", description = "Darken Undiscovered Rooms", enabled = true)
    val darkenMultiplier = NumberSetting("Darken Multiplier", default = 0.4, min = 0.1, max = 1.0, increment = 0.1)
    val playerHeads = SelectorSetting("Player Names", description = "Show player name under player head", default = "OFF", options = arrayOf("OFF","Holding Leap","Always"))
    val vanillaHead = BooleanSetting("Vanilla Marker", description = "Vanilla Marker for yourself")
    val roomNames = SelectorSetting("Room Names", description = "Shows names of rooms on map.", default = "Puzzles / Trap", options = arrayOf("None", "Puzzles / Trap", "All"))
    val textScale = NumberSetting("Text Scale", default = 0.75, min = 0.0, max = 2.0, increment = 0.1)
    val nameScale = NumberSetting("Names Scale", default = 0.8, min = 0.0, max = 2.0, increment = 0.1)
    val playerHeadsScale = NumberSetting("Heads Scale", default = 1.0, min = 0.0, max = 2.0, increment = 0.1)
    val borderWidth = NumberSetting("Map Border Width", default = 3.0, min = 0.0, max = 10.0)
    val checkMark = SelectorSetting("Room Checkmarks", description = "Adds room checkmarks based on room state.", default = "Default", options = arrayOf("Default","NEU"))
    val doorESP = SelectorSetting("Door ESP", default = "ALL", options = arrayOf("OFF","First","ALL"))
    val doorOutlineWidth = NumberSetting("ESP outline width", default = 3.0, min = 1.0, max = 10.0)
    val noKeyColor = ColorSetting("No Key Color", description = "ESP color when you don't have a key", default = Color(255, 0, 0))
    val keyColor = ColorSetting("Key Color", description = "ESP color when you have a key", default = Color(0, 255, 0))

    val mapX = NumberSetting("MapX", default = 10.0, hidden = true)
    val mapY = NumberSetting("MapY", default = 10.0, hidden = true)
    val mapScale = NumberSetting("MapScale", default = 1.0,min = 0.1,max = 4.0, hidden = true)


    init {
        addSettings(
            mapEnabled,
            hideInBoss,
            borderWidth,
            scanMimic,
            highlightMimic,
            darkenUndiscovered,
            darkenMultiplier,
            playerHeads,
            vanillaHead,
            playerHeadsScale,
            roomNames,
            nameScale,
            textScale,
            checkMark,
            doorESP,
            doorOutlineWidth,
            noKeyColor,
            keyColor,
            mapX,
            mapY,
            mapScale
        )
    }

    override fun keyBind() {}

    override fun onEnable() {}

    override fun onDisable() {}

    private val lines
        get() = Score.lines
    val h: Int
        get() = if (Score.showRunInfo.enabled) (128 + lines.size * 2.5).toInt() else 128

    class MapElement : HudElement(mapX,mapY,135,h,mapScale) {

        override fun shouldRender(): Boolean {
            if (hideInBoss.enabled && Location.inBoss) return false
            return mapEnabled.enabled && inDungeons
        }

        private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
        private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
        private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
        private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
        private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
        private val defaultCross = ResourceLocation("funnymap", "default/cross.png")

        override fun render() {
            mc.mcProfiler.startSection("border")

            RenderUtil.renderRect(0.0, 0.0, 128.0, if (Score.showRunInfo.enabled) 128.0 + Score.lines.size * 2.5 else 128.0, Colors.mapBackground.value)

            RenderUtil.renderRectBorder(
                0.0,
                0.0,
                128.0,
                if (Score.showRunInfo.enabled) 128.0 + Score.lines.size * 2.5 else 128.0,
                borderWidth.value,
                Colors.mapBorder.value
            )

            mc.mcProfiler.endSection()

            mc.mcProfiler.startSection("rooms")
            renderRooms()
            mc.mcProfiler.endStartSection("text")
            renderText()
            mc.mcProfiler.endStartSection("heads")
            renderPlayerHeads()
            mc.mcProfiler.endSection()

            if (Score.showRunInfo.enabled) {
                mc.mcProfiler.startSection("footer")
                renderRunInformation()
                mc.mcProfiler.endSection()
            }
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

                    if (tile.state == RoomState.UNDISCOVERED) {
                        if (darkenUndiscovered.enabled) {
                            color = color.run {
                                Color(
                                    (red * (1 - darkenMultiplier.value)).toInt(),
                                    (green * (1 - darkenMultiplier.value)).toInt(),
                                    (blue * (1 - darkenMultiplier.value)).toInt(),
                                    alpha
                                )
                            }
                        }
                    }

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

                        else -> drawRoomConnector(xOffset, yOffset, connectorSize, tile is Door, !xEven, color)
                    }
                }
            }
            GlStateManager.popMatrix()
        }

        private fun renderText() {
            GlStateManager.pushMatrix()
            GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

            val connectorSize = mapRoomSize shr 2
            val checkmarkSize = when (checkMark.selected) {
                "Default" -> 8.0
                else -> 10.0 // neu
            }

            Dungeon.uniqueRooms.forEach { (room, pos) ->
                val xOffset = (pos.first shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (pos.second shr 1) * (mapRoomSize + connectorSize)

                    getCheckmark(room.state, checkMark.selected)?.let {
                        GlStateManager.enableAlpha()
                        GlStateManager.color(255f, 255f, 255f, 255f)
                        mc.textureManager.bindTexture(it)

                        RenderUtil.drawTexturedQuad(
                            xOffset + (mapRoomSize - checkmarkSize) / 2,
                            yOffset + (mapRoomSize - checkmarkSize) / 2,
                            checkmarkSize,
                            checkmarkSize
                        )
                        GlStateManager.disableAlpha()
                    }

                val color = when (room.state) {
                    RoomState.GREEN -> 0x55ff55
                    RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                    else -> 0xaaaaaa
                }

                val name = mutableListOf<String>()

                if (RandomStuff.peekBind.isKeyDown || (roomNames.selected != "None" && room.data.type.equalsOneOf(RoomType.PUZZLE, RoomType.TRAP)) || (roomNames.selected == "ALL" && room.data.type.equalsOneOf(RoomType.NORMAL, RoomType.RARE, RoomType.CHAMPION))) name.addAll(room.data.name.split(" "))
                // Offset + half of roomsize
                RenderUtil.renderCenteredText(
                    name,
                    xOffset + (mapRoomSize shr 1),
                    yOffset + (mapRoomSize shr 1),
                    color
                )
            }
            GlStateManager.popMatrix()
        }

        private fun getCheckmark(state: RoomState, type: String): ResourceLocation? {
            return when (type) {
                "Default" -> when (state) {
                    RoomState.CLEARED -> defaultWhite
                    RoomState.GREEN -> defaultGreen
                    RoomState.FAILED -> defaultCross
                    else -> null
                }

                "NEU" -> when (state) {
                    RoomState.CLEARED -> neuWhite
                    RoomState.GREEN -> neuGreen
                    RoomState.FAILED -> neuCross
                    else -> null
                }

                else -> null
            }
        }

        private fun renderPlayerHeads() {
            try {
                if (Dungeon.dungeonTeammates.isEmpty()) {
                    drawPlayerHead(mc.thePlayer.name, DungeonPlayer(mc.thePlayer.locationSkin).apply { yaw = mc.thePlayer.rotationYaw })
                } else {
                    Dungeon.dungeonTeammates.forEach { (name, teammate) ->
                        if (!teammate.dead) drawPlayerHead(name, teammate)
                    }
                }
            } catch (_: ConcurrentModificationException) {
            }
        }

        private fun drawRoomConnector(
            x: Int,
            y: Int,
            doorWidth: Int,
            doorway: Boolean,
            vertical: Boolean,
            color: Color,
        ) {
            val doorwayOffset = if (mapRoomSize == 16) 5 else 6
            val width = if (doorway) 6 else mapRoomSize
            var x1 = if (vertical) x + mapRoomSize else x
            var y1 = if (vertical) y else y + mapRoomSize
            if (doorway) {
                if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
            }
            RenderUtil.renderRect(
                x1.toDouble(),
                y1.toDouble(),
                (if (vertical) doorWidth else width).toDouble(),
                (if (vertical) width else doorWidth).toDouble(),
                color
            )
        }

            private fun renderRunInformation() {
                GlStateManager.pushMatrix()
                GlStateManager.translate(64f, 128f, 0f)
                GlStateManager.scale(2.0 / 3.0, 2.0 / 3.0, 1.0)
                val fr = mc.fontRendererObj
                val lineOne = lines.takeWhile { it != "split" }.joinToString(separator = "    ")
                val lineTwo =
                    lines.takeWhile { it != "split1" }.takeLastWhile { it != "split" }.joinToString(separator = "    ")
                val lineThree = lines.takeLastWhile { it != "split" && it != "split1" }.joinToString(separator = "    ")
                val l1sw = -fr.getStringWidth(lineOne) / 2
                val l2Sw = -fr.getStringWidth(lineTwo) / 2
                renderText(lineOne, l1sw, 0)
                renderText(lineTwo, l2Sw, if (l1sw == 0) 0 else 9)
                renderText(
                    lineThree,
                    -fr.getStringWidth(lineThree) / 2,
                    if (l2Sw.and(l1sw) == 0) 0 else if (l2Sw.or(l1sw) == 0) 9 else 18
                )

                GlStateManager.popMatrix()
            }


            private fun drawPlayerHead(name: String, player: DungeonPlayer) {
                GlStateManager.pushMatrix()
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
                GlStateManager.scale(playerHeadsScale.value, playerHeadsScale.value, 1.0)

                if (vanillaHead.enabled && (player.isPlayer || name == mc.thePlayer.name)) {
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
                    RenderUtil.renderRectBorder(-6.0, -6.0, 12.0, 12.0, 1.0, Color(0, 0, 0, 255))

                    preDraw()
                    GlStateManager.enableTexture2D()
                    GlStateManager.color(1f, 1f, 1f, 1f)

                    mc.textureManager.bindTexture(player.skin)

                    Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
                    if (player.renderHat) Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
                    postDraw()
                }

                // Handle player names
                if (name != mc.thePlayer.name && (RandomStuff.peekBind.isKeyDown || playerHeads.isSelected("Always") || playerHeads.isSelected(
                        "Holding Leap"
                    ) && mc.thePlayer.heldItem?.itemID.equalsOneOf(
                        "SPIRIT_LEAP",
                        "INFINITE_SPIRIT_LEAP",
                        "HAUNT_ABILITY"
                    ))
                ) {
                    GlStateManager.rotate(-player.yaw + 180f, 0f, 0f, 1f)
                    GlStateManager.translate(0f, 10f, 0f)
                    GlStateManager.scale(nameScale.value, nameScale.value, 1.0)
                    renderText(name, -mc.fontRendererObj.getStringWidth(name) / 2, 0)
                }
                GlStateManager.popMatrix()
            }
        }
    }