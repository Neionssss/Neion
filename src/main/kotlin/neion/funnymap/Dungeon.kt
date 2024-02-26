package neion.funnymap

import cc.polyfrost.oneconfig.libs.universal.UChat
import neion.Config
import neion.FMConfig
import neion.Neion.Companion.mc
import neion.events.ChatEvent
import neion.features.dungeons.EditMode
import neion.funnymap.map.*
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.TextUtils.matchesAny
import net.minecraft.block.Block
import net.minecraft.event.ClickEvent
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Dungeon {

    private val shouldScan: Boolean
        get() = !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && Location.dungeonFloor != -1
    val dungeonList = Array<Tile?>(121) { null }
    val uniqueRooms = mutableListOf<Pair<Room, Pair<Int, Int>>>()
    var roomCount = 0
    var cryptCount = 0
    var secretCount = 0
    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var lastScanTime = 0L
    private var isScanning = false
    private var hasScanned = false
    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()
    val doors = mutableListOf<Door>()


    // https://i.imgur.com/NutLQZQ.png
    fun getMimicRoom(): Room? {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }.groupingBy {
            ScanUtils.getRoomFromPos(it.pos)
        }.eachCount().forEach { (room, trappedChests) ->
            return dungeonList.filterIsInstance<Room>().find { it == room && it.data.trappedChests < trappedChests }
        }
        return null
    }

    fun onTick() {
        if (!Location.inDungeons) return
        if (FMConfig.scanMimic && !RunInformation.mimicFound && Location.dungeonFloor.equalsOneOf(6, 7) && getMimicRoom() != null) {
            if (FMConfig.mimicInfo) UChat.chat("&7Mimic Room: &c${getMimicRoom()?.data?.name}")
            RunInformation.mimicFound = true
        }

        if (!MapUtils.calibrated()) MapUtils.calibrated()
        MapUpdate.updateRooms()
        RunInformation.checkMimicDeath()
        RunInformation.updateScore()
        MapUtils.getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updatePuzzleCount(it)
        }
        if (shouldScan) scan()
        if (Config.preBlocks) {
            (EditMode.getCurrentRoomPair() ?: return).run roomPair@{
                ScanUtils.extraRooms[this.first.data.name]?.run {
                    this.preBlocks.forEach { (blockID, posList) ->
                        posList.forEach {
                            mc.theWorld.setBlockState(
                                ScanUtils.getRealPos(it, this@roomPair),
                                ScanUtils.getStateFromIDWithRotation(Block.getStateById(blockID), this@roomPair.second))
                        }
                    }
                }
            }
        }
    }

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
                if (dungeonList[x + z * 11] != null) continue
                dungeonList[z * 11 + x] = scanRoom(xPos, zPos, z, x)
            }
        }

        if (allChunksLoaded) {
            roomCount = dungeonList.filter { it is Room && !it.isSeparator }.size
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
                    val duplicateRoom = uniqueRooms.firstOrNull { it.first.data.name == data.name }
                    if (duplicateRoom == null) {
                        uniqueRooms.add(this to (column to row))
                        cryptCount += data.crypts
                        secretCount += data.secrets
                        when (data.type) {
                            RoomType.TRAP -> RunInformation.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> Puzzle.fromName(data.name)
                                ?.let { RunInformation.puzzles.putIfAbsent(it, false) }

                            RoomType.FAIRY -> MapUpdate.rooms[this] = 0
                            RoomType.ENTRANCE -> {
                                listOf(
                                    0 to -7,
                                    7 to 0,
                                    0 to 7,
                                    -7 to 0
                                ).forEachIndexed { index, pair ->
                                    if (mc.theWorld.getBlockState(
                                            BlockPos(
                                                x + pair.first,
                                                70,
                                                z + pair.second
                                            )
                                        ).block == Blocks.air
                                    ) MapUpdate.rooms[this] = index * 90
                                }
                            }


                            else -> {}
                        }
                    } else if (x < duplicateRoom.first.x || (x == duplicateRoom.first.x && z < duplicateRoom.first.z)) {
                        uniqueRooms.remove(duplicateRoom)
                        uniqueRooms.add(this to (column to row))
                    }
                }
            }
            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> {
                dungeonList[column - 1 + (row - 1) * 11]?.let {
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
                ).also { doors.add(it) }
            }

            // Connection between large rooms
            else -> {
                dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it !is Room) return null
                    if (it.data.type == RoomType.ENTRANCE) Door(x, z, DoorType.ENTRANCE)
                    else Room(x, z, it.data).apply { isSeparator = true }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!FMConfig.highLightMimic || !Location.inDungeons) return
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 && ScanUtils.getRoomFromPos(it.pos) == getMimicRoom() }.forEach {
            if (getMimicRoom() == EditMode.getCurrentRoomPair()?.first) RenderUtil.drawBlockBox(it.pos, Color.blue, outline = true, fill = false, esp = true)
        }
    }


    fun reset() {
        dungeonList.fill(null)
        uniqueRooms.clear()
        roomCount = 0
        cryptCount = 0
        secretCount = 0
        dungeonTeammates.clear()
        doors.clear()
        PlayerTracker.roomClears.clear()
        hasScanned = false
        MapUpdate.rooms.clear()
        RunInformation.reset()
    }
}
