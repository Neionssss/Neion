package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import kotlin.math.min

object MapUpdate {


    val rooms = mutableMapOf<Room, Int>()
    val roomClears: MutableMap<RoomData, Set<String>> = mutableMapOf()

    fun updateRooms() {
        for (x in 0..10) {
            for (z in 0..10) {
                val mapTile = mapTile(x, z) ?: return
                val roomTile = Dungeon.dungeonList[z * 11 + x]

                if (mapTile.state.ordinal < roomTile?.state?.ordinal!!) {
                    val room = roomTile as? Room ?: return
                    if (room.state.equalsOneOf(
                            RoomState.CLEARED,
                            RoomState.GREEN
                        ) && mapTile.state != RoomState.CLEARED
                    ) roomClears[room.data] =
                        Dungeon.players.map { Pair(it.value.formattedName, it.value.getCurrentRoom()) }
                            .filter { it.first != "" && it.second == room }
                            .map { it.first }
                            .toSet()

                    room.state = mapTile.state

                    listOf(-15 to -15, 15 to -15, 15 to 15, -15 to 15).mapIndexed { index, pair ->
                        val height = mc.theWorld.getChunkFromChunkCoords(room.x.shr(4), room.z shr 4).getHeightValue(room.x and 15, room.z and 15)
                        if (mc.theWorld.getBlockState(
                                BlockPos(
                                    room.x + pair.first,
                                    min(height, height - 1),
                                    room.z + pair.second
                                )
                            ).block == Block.getBlockById(159)
                        ) rooms.putIfAbsent(room, index * 90)
                    }
                }
            }
        }

        Dungeon.dungeonList.filterIsInstance<Door>()
            .filter { it.type.equalsOneOf(DoorType.WITHER, DoorType.ENTRANCE, DoorType.BLOOD) && !it.opened }.forEach {
            if (mc.theWorld.getChunkFromChunkCoords(it.x shr 4, it.z shr 4).isLoaded && mc.theWorld.getBlockState(
                    BlockPos(it.x, 69, it.z)
                ).block == Blocks.air
            ) it.opened = true
        }
    }

    private fun mapTile(x: Int, z: Int): Tile? {
        val mapColors = MapUtils.getMapData()?.colors ?: return null
        val centerTileColors = ByteArray(121)
        val sideTileColors = ByteArray(121)
        val worldX = Dungeon.STARTX + x * (Dungeon.ROOMSIZE shr 1)
        val worldZ = Dungeon.STARTZ + z * (Dungeon.ROOMSIZE shr 1)
        val halfRoom = MapUtils.mapRoomSize / 2
        val halfTileWidth = halfRoom + 2

        val mapX = MapUtils.startCorner.first + halfRoom + x * halfTileWidth
        val mapY = MapUtils.startCorner.second + halfRoom + z * halfTileWidth

        if (mapX or mapY >= 128) return null

        centerTileColors[z * 11 + x] = mapColors[mapY * 128 + mapX]
        sideTileColors[z * 11 + x] = mapColors[
            if (x % 2 == 0 && z % 2 == 0) mapX - halfRoom * 128 + mapY - halfRoom
            else if (z % 2 == 1) mapY * 128 + mapX - 4
            else (mapY - 4) * 128 + mapX]

        val centerColor = centerTileColors[z * 11 + x].toInt()
        val sideColor = sideTileColors[z * 11 + x].toInt()

        if (centerColor == 0) return null

        val unknownRoom = Room(worldX, worldZ, RoomData("Unknown", RoomType.fromColor(sideColor) ?: return null))

        return when (x % 2) {
            0 -> when (z % 2) {
                0 -> unknownRoom.apply {
                    state = when (centerColor) {
                        18 -> when (data.type) {
                            RoomType.BLOOD -> RoomState.DISCOVERED
                            RoomType.PUZZLE -> RoomState.FAILED
                            else -> state
                        }

                        30 -> RoomState.GREEN
                        34 -> RoomState.CLEARED
                        else -> RoomState.DISCOVERED
                    }
                }

                else -> Door(worldX, worldZ).apply {
                    if (centerColor != 85) state = RoomState.DISCOVERED
                }
            }

            else -> unknownRoom.apply { state = RoomState.DISCOVERED }
        }
    }
}