package neion.funnymap

import neion.Neion.Companion.mc
import neion.features.dungeons.EditMode
import neion.features.dungeons.PreBlocks
import neion.funnymap.map.*
import neion.ui.Mapping
import neion.utils.ExtrasConfig
import neion.utils.Location
import neion.utils.MapUtils
import neion.utils.RenderUtil
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object Dungeon {

    val dungeonList = Array<Tile?>(121) { null }
    val uniqueRooms = mutableListOf<Pair<Room, Pair<Int, Int>>>()
    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var isScanning = false
    private var hasScanned = false
    var fairyPos: Pair<Int,Int>? = null
    val dungeonTeammates = ConcurrentHashMap<String, DungeonPlayer>()

    fun onTick() {
        if (!Location.inDungeons) return
        ScoreCalculation.updateScore()
        if (!Location.inBoss) {
            if (!MapUtils.calibrateMap()) MapUtils.calibrateMap()
            MapUpdate.updateRooms()
            MapUtils.getDungeonTabList()?.let {
                MapUpdate.updatePlayers(it)
                RunInformation.updatePuzzles(it)
            }
        }
        if (!isScanning && !hasScanned && Location.dungeonFloor != -1) scan()
        if (PreBlocks.enabled) EditMode.getCurrentRoomPair()?.run {
            ExtrasConfig.extraRooms[first.data.name]?.preBlocks?.forEach { (blockID, posList) ->
                posList.forEach {
                    mc.theWorld.setBlockState(
                        MapUtils.getRealPos(it, roomPair = this),
                        MapUtils.getStateFromIDWithRotation(Block.getStateById(blockID), rotation = this.second)
                    )
                }
            }
        }
    }

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        // Scans the dungeon in a 11x11 grid.
        for (column in 0..10) {
            for (row in 0..10) {
                // Translates the grid index into world position.
                val xPos = startX + column * (roomSize shr 1)
                val zPos = startZ + row * (roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) allChunksLoaded = false

                // This room has already been added in a previous scan.
                if (dungeonList[column + row * 11] != null) continue
                dungeonList[row * 11 + column] = scanRoom(xPos, zPos, row, column)
            }
        }

        if (allChunksLoaded) hasScanned = true
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
                val roomCore = MapUtils.getCore(x, z)
                Room(x, z, MapUtils.getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    if (data.type == RoomType.FAIRY) fairyPos = Pair(x,z)
                    if (Mapping.scanMimic.enabled && !RunInformation.mimicFound && Location.dungeonFloor.equalsOneOf(6, 7) && hasMimic) RunInformation.mimicFound = true
                    // Checks if a room with the same name has already been scanned.
                    val duplicateRoom = uniqueRooms.firstOrNull { it.first.data.name == data.name }
                    if (duplicateRoom == null) uniqueRooms.add(this to (column to row))
                    else if (x < duplicateRoom.first.x || (x == duplicateRoom.first.x && z < duplicateRoom.first.z)) {
                        uniqueRooms.remove(duplicateRoom)
                        uniqueRooms.add(this to (column to row))
                    }
                }
            }
            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> dungeonList[column - 1 + (row - 1) * 11]?.apply {
                if (this is Room) Room(x, z, data)
            }

            // Doorway between rooms
            // Old trap has a single block at 82 / New also does unfortunately
            height.equalsOneOf(74, 82) -> Door(
                    x, z,
                    // Finds door type from door block
                    when (mc.theWorld.getBlockState(BlockPos(x, 70, z)).block) {
                        Blocks.coal_block -> DoorType.WITHER
                        Blocks.monster_egg -> DoorType.ENTRANCE
                        Blocks.stained_hardened_clay -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    })

            // Connection between large rooms
            else -> dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].apply {
                if (this is Room && data.type == RoomType.ENTRANCE) Door(x, z, DoorType.ENTRANCE)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!Mapping.highlightMimic.enabled || !Location.inDungeons) return
        val roomie = dungeonList.filterIsInstance<Room>().find { it.hasMimic }
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 && MapUtils.getRoomFromPos(it.pos) == roomie }.forEach {
            if (roomie == EditMode.getCurrentRoomPair()?.first) RenderUtil.drawBlockBox(it.pos, Color.blue, outline = true, fill = false, esp = true)
        }
    }


    fun reset() {
        dungeonList.fill(null)
        uniqueRooms.clear()
        dungeonTeammates.clear()
        hasScanned = false
        RunInformation.reset()
    }
}