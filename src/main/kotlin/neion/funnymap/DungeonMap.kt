package neion.funnymap

import neion.funnymap.map.*

class DungeonMap(mapColors: ByteArray) {
    private var centerColors = ByteArray(121)
    private var sideColors = ByteArray(121)

    init {
        val halfRoom = MapUtils.mapRoomSize / 2
        val halfTile = halfRoom + 2
        val startX = MapUtils.startCorner.first + halfRoom
        val startY = MapUtils.startCorner.second + halfRoom

        for (y in 0..10) {
            for (x in 0..10) {
                val mapX = startX + x * halfTile
                val mapY = startY + y * halfTile

                if (mapX >= 128 || mapY >= 128) continue

                centerColors[y * 11 + x] = mapColors[mapY * 128 + mapX]

                val sideIndex = if (x % 2 == 0 && y % 2 == 0) {
                    val topX = mapX - halfRoom
                    val topY = mapY - halfRoom
                    topY * 128 + topX
                } else if (y % 2 == 1) mapY * 128 + mapX - 4 else (mapY - 4) * 128 + mapX

                sideColors[y * 11 + x] = mapColors[sideIndex]
            }
        }
    }

    fun scanTile(arrayX: Int, arrayY: Int, worldX: Int, worldZ: Int): Tile? {
        fun unknown(x: Int, z: Int, type: RoomType): Room {
            return Room(x, z, RoomData("Unknown", type, emptyList(), 0, 0, 0))
        }
        val centerColor = centerColors[arrayY * 11 + arrayX].toInt()
        val sideColor = sideColors[arrayY * 11 + arrayX].toInt()
        if (centerColor == 0) return null
        return if (arrayX % 2 == 0 && arrayY % 2 == 0) {
            val type = RoomType.fromMapColor(sideColor) ?: return null
            unknown(worldX,worldZ,type).apply {
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
            if (sideColor == 0) {
                val type = DoorType.fromMapColor(centerColor) ?: return null
                Door(worldX, worldZ, type).apply { if (centerColor != 85) state = RoomState.DISCOVERED }
            } else {
                val type = RoomType.fromMapColor(sideColor) ?: return null
                unknown(worldX,worldZ,type).apply {
                    state = RoomState.DISCOVERED
                    isSeparator = true
                }
            }
        }
    }
}
