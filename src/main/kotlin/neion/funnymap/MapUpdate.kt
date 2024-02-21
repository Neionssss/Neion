package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.funnymap.map.MapUtils.mapX
import neion.funnymap.map.MapUtils.mapZ
import neion.funnymap.map.MapUtils.yaw
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.block.Block
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import kotlin.math.min
import kotlin.math.roundToInt

object MapUpdate {
    val rooms = mutableMapOf<Room, Int>()
    fun getPlayers() {
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with((MapUtils.getDungeonTabList() ?: return)[i]) {
                val name = second.stripControlCodes().trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    // https://i.imgur.com/kzz1RwC.png
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                        colorPrefix = second.substringBefore(name, "f").last()
                        this.name = name
                        icon = "icon-$iconNum"
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return
        // Update map icons
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.substringAfterLast("] ").split(" ")[0]
            if (name == "") continue
            Dungeon.dungeonTeammates[name]?.run {
                dead = tabText.contains("(DEAD)")
                if (dead) icon = ""
                else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
                if (!playerLoaded) mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }

                val room = getCurrentRoom()?.data?.name
                val time = System.currentTimeMillis() - Dungeon.Info.startTime
                if (room != null && time > 1000) {
                    if (lastRoom == "") lastRoom = room
                    else if (lastRoom != room) {
                        roomVisits.add(Pair(time - lastTime, lastRoom))
                        lastTime = time
                        lastRoom = room
                    }
                }
            }
        }

        Dungeon.dungeonTeammates.forEach { (_, player) ->
            MapUtils.getMapData()?.mapDecorations?.entries?.find { it.key == player.icon }
                ?.let { (_, vec4b) ->
                    player.isPlayer = vec4b.func_176110_a().toInt() == 1
                    player.mapX = vec4b.mapX
                    player.mapZ = vec4b.mapZ
                    player.yaw = vec4b.yaw
                }
            if (player.isPlayer) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX = ((mc.thePlayer.posX - Dungeon.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ = ((mc.thePlayer.posZ - Dungeon.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return
        val centerColors = ByteArray(121)
        val sideColors = ByteArray(121)

        for (x in 0..10) {
            for (z in 0..10) {
                val halfRoom = MapUtils.mapRoomSize / 2
                val halfTile = halfRoom + 2
                val mapX = MapUtils.startCorner.first + halfRoom + x * halfTile
                val mapY = MapUtils.startCorner.second + halfRoom + z * halfTile
                if (mapX.or(mapY) >= 128) continue
                centerColors[z * 11 + x] = mapColors[mapY * 128 + mapX]

                val sideIndex = if (x % 2 == 0 && z % 2 == 0) {
                    val topX = mapX - halfRoom
                    val topY = mapY - halfRoom
                    topY * 128 + topX
                } else if (z % 2 == 1) mapY * 128 + mapX - 4 else (mapY - 4) * 128 + mapX

                sideColors[z * 11 + x] = mapColors[sideIndex]
                fun mapTile(): Tile? {
                    val worldX = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                    val worldZ = Dungeon.startZ + z * (Dungeon.roomSize shr 1)
                    fun unknown(type: RoomType) = Room(worldX, worldZ, RoomData("Unknown", type, emptyList(), 0, 0, 0))

                    val centerColor = centerColors[z * 11 + x].toInt()
                    val sideColor = sideColors[z * 11 + x].toInt()
                    if (centerColor == 0) return null
                    return if (x % 2 == 0 && z % 2 == 0) {
                        val type = RoomType.fromMapColor(sideColor) ?: return null
                        unknown(type).apply {
                            state = when (centerColor) {
                                18 -> when (type) {
                                    RoomType.BLOOD -> RoomState.DISCOVERED
                                    RoomType.PUZZLE -> RoomState.FAILED
                                    else -> state
                                }

                                30 -> when (type) {
                                    RoomType.ENTRANCE -> RoomState.DISCOVERED
                                    else -> RoomState.GREEN
                                }

                                34 -> RoomState.CLEARED
                                else -> RoomState.DISCOVERED
                            }
                        }
                    } else {
                        if (sideColor == 0) Door(worldX, worldZ, DoorType.fromMapColor(centerColor) ?: return null).apply { if (centerColor != 85) state = RoomState.DISCOVERED }
                        else {
                            unknown(RoomType.fromMapColor(sideColor) ?: return null).apply {
                                state = RoomState.DISCOVERED
                                isSeparator = true
                            }
                        }
                    }
                }
                val room = Dungeon.Info.dungeonList[z * 11 + x] ?: continue
                val mapTile = mapTile() ?: continue
                if (mapTile.state.ordinal < room.state.ordinal) {
                    (room as? Room)?.let {
                            listOf(-15 to -15, 15 to -15, 15 to 15, -15 to 15).forEachIndexed { index, pair ->
                                val height = mc.theWorld.getChunkFromChunkCoords(it.x shr 4, it.z shr 4).getHeightValue(it.x and 15, it.z and 15)
                                if (mc.theWorld.getBlockState(BlockPos(it.x + pair.first, min(height, height - 1), it.z + pair.second)).block == Block.getBlockById(159) && !rooms.contains(it)) rooms[it] = index * 90
                            }
                        }
                    PlayerTracker.roomStateChange(room, room.state, mapTile.state)
                    room.state = mapTile.state
                }
                if (room is Door && room.type.equalsOneOf(DoorType.WITHER,DoorType.BLOOD) && !room.opened && mc.theWorld.getChunkFromChunkCoords(room.x shr 4, room.z shr 4).isLoaded && mc.theWorld.getBlockState(BlockPos(room.x, 69, room.z)).block == Blocks.air) room.opened = true
            }
        }
    }
}