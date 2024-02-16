package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location.dungeonFloor
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import kotlin.math.min


object DungeonScan {

    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var lastScanTime = 0L
    var isScanning = false
    var hasScanned = false

    val shouldScan: Boolean
        get() = !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && dungeonFloor != -1
    val rooms = mutableMapOf<Room, Int>()

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        // Scans the dungeon in a 11x11 grid.
        for (x in 0..10) {
            for (z in 0..10) {
                // Translates the grid index into world position.
                val xPos = startX + x * (roomSize shr 1)
                val zPos = startZ + z * (roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) allChunksLoaded = false

                // This room has already been added in a previous scan.
                if (Dungeon.Info.dungeonList[x + z * 11] != null)  continue

                scanRoom(xPos, zPos, z, x)?.let {
                    Dungeon.Info.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allChunksLoaded) {
            Dungeon.Info.roomCount = Dungeon.Info.dungeonList.filter { it is Room && !it.isSeparator }.size
            hasScanned = true
        }

        lastScanTime = System.currentTimeMillis()
        isScanning = false
    }

    private fun scanRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val height = mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
        if (height == 0) return null

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            // Scanning a room
            rowEven && columnEven -> {
                val roomCore = ScanUtils.getCore(x, z)
                Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    // Checks if a room with the same name has already been scanned.
                    val duplicateRoom = Dungeon.Info.uniqueRooms.firstOrNull { it.first.data.name == data.name }
                    if (duplicateRoom == null) {
                        Dungeon.Info.uniqueRooms.add(this to (column to row))
                        Dungeon.Info.cryptCount += data.crypts
                        Dungeon.Info.secretCount += data.secrets
                        when (data.type) {
                            RoomType.TRAP -> Dungeon.Info.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> Puzzle.fromName(data.name)
                                ?.let { Dungeon.Info.puzzles.putIfAbsent(it, false) }

                            else -> {}
                        }
                        reRotate(this)
                    } else if (x < duplicateRoom.first.x || (x == duplicateRoom.first.x && z < duplicateRoom.first.z)) {
                        Dungeon.Info.uniqueRooms.remove(duplicateRoom)
                        Dungeon.Info.uniqueRooms.add(this to (column to row))
                    }
                }
            }
            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> {
                Dungeon.Info.dungeonList[column - 1 + (row - 1) * 11]?.let {
                    if (it is Room) Room(x, z, it.data).apply { isSeparator = true } else null
                }
            }

            // Doorway between rooms
            // Old trap has a single block at 82 / New also does unfortunately
            height.equalsOneOf(74, 82) -> {
                Door(
                    x, z,
                    // Finds door type from door block
                        when (mc.theWorld.getBlockState(BlockPos(x, 70, z)).block) {
                            Blocks.coal_block -> DoorType.WITHER
                            Blocks.monster_egg -> DoorType.ENTRANCE
                            Blocks.stained_hardened_clay -> DoorType.BLOOD
                            else -> DoorType.NORMAL
                        }
                ).also { Dungeon.doors.add(it) }
            }

            // Connection between large rooms
            else -> {
                Dungeon.Info.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it !is Room) return null
                    if (it.data.type == RoomType.ENTRANCE) Door(x, z, DoorType.ENTRANCE)
                    else Room(x, z, it.data).apply { isSeparator = true }
                }
            }
        }
    }

    fun reRotate(room: Room) {
        when (room.data.type) {
            RoomType.FAIRY -> rooms[room] = 0
            RoomType.ENTRANCE -> {
                listOf(
                    0 to -7,
                    7 to 0,
                    0 to 7,
                    -7 to 0
                ).forEachIndexed { index, pair ->
                    if (mc.theWorld.getBlockState(
                            BlockPos(
                                room.x + pair.first,
                                70,
                                room.z + pair.second
                            )
                        ).block == Blocks.air
                    ) rooms[room] = index * 90
                }
            }

            else -> listOf(-15 to -15, 15 to -15, 15 to 15, -15 to 15).forEachIndexed { index, pair ->
                val height = mc.theWorld.getChunkFromChunkCoords(room.x shr 4, room.z shr 4).getHeightValue(room.x and 15, room.z and 15)
                if (mc.theWorld.getBlockState(
                        BlockPos(
                            room.x + pair.first,
                            min(height, height - 1),
                            room.z + pair.second
                        )
                    ).block == Block.getBlockById(159)
                ) rooms[room] = index * 90
            }
        }
    }
}