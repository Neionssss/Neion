package neion.funnymap

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import neion.Config
import neion.Neion.Companion.mc
import neion.features.dungeons.EditMode
import neion.funnymap.RunInformation.firstResult
import neion.funnymap.map.*
import neion.utils.Location
import neion.utils.TextUtils.stripControlCodes
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import kotlin.math.roundToInt

object Dungeon {

    val dungeonList = Array<Tile?>(121) { null }
    val uniqueRooms = mutableListOf<Pair<Room, Pair<Int, Int>>>()
    const val ROOMSIZE = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val STARTX = -185
    const val STARTZ = -185

    private var lastScanTime = 0L
    private var isScanning = false
    private var hasScanned = false
    val players = mutableMapOf<String, DungeonPlayer>()
    val roomList: Set<RoomData> = Gson().fromJson(mc.resourceManager.getResource(ResourceLocation("funnymap", "rooms.json")).inputStream.bufferedReader(), object : TypeToken<Set<RoomData>>() {}.type)
    private var mimicOpenTime = 0L
    private var mimicPos: BlockPos? = null

    fun onTick() {
        if (!Location.inDungeons) return
        if (!MapUtils.calibrated()) MapUtils.calibrated()
        MapUpdate.updateRooms()
        RunInformation.updateScore()
        fetchTabInfo()
        if (!isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && Location.dungeonFloor != -1) scanRoom()
        if (Config.preBlocks) MapUtils.extraRooms[EditMode.getCurrentRoomPair()?.first?.data?.name!!]?.preBlocks?.map { p ->
            p.value.forEach { mc.theWorld.setBlockState(MapUtils.getRealPos(it), MapUtils.getStateIDWithRotation(Block.getStateById(p.key))) }
        }
    }

    private fun scanRoom() {
        isScanning = true

        for (column in 0..10) {
            for (row in 0..10) {
                val xPos = STARTX + column * (ROOMSIZE shr 1)
                val zPos = STARTZ + row * (ROOMSIZE shr 1)

                val chunk = mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4)

                hasScanned = chunk.isLoaded

                val height = chunk.getHeightValue(xPos and 15, zPos and 15)

                if (height == 0) return

                val rowEven = row and 1 == 0
                val columnEven = column and 1 == 0

                if (dungeonList[column + row * 11] == null) when {
                    // Scanning a room
                    rowEven && columnEven -> {
                        val newRoom = Room(xPos, zPos)
                        val duplicateRoom = uniqueRooms.firstOrNull { it.first.data.name == newRoom.data.name }

                        if (duplicateRoom == null) uniqueRooms.add(newRoom to (column to row)) else {
                            uniqueRooms.remove(duplicateRoom)
                            if (newRoom.x < duplicateRoom.first.x || (newRoom.x == duplicateRoom.first.x && newRoom.z < duplicateRoom.first.z)) uniqueRooms.add(newRoom to (column to row))
                        }

                        dungeonList[row * 11 + column] = newRoom
                    }

                    // Trap has block at 82
                    height.equalsOneOf(74, 82) -> dungeonList[row * 11 + column] = Door(xPos, zPos)

                    // Connection between large rooms
                    else -> if ((dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column] as? Room)?.data?.type == RoomType.ENTRANCE) dungeonList[row * 11 + column] = Door(xPos, zPos, DoorType.ENTRANCE)
                }
            }
        }

        lastScanTime = System.currentTimeMillis()
        isScanning = false
    }

    private fun fetchTabInfo() {
        val list = MapUtils.getDungeonTabList() ?: return
        val tabText = stripControlCodes(list.find { s -> s.equalsOneOf(5, 9, 13, 17, 1) }?.second).trim()
        val name = tabText.substringAfterLast("] ").split(" ")[0]
        if (name != "") players[name]?.run {
            dead = tabText.contains("(DEAD)")
            icon = if (dead) "" else "icon-$name"
            if (!playerLoaded) setData(mc.theWorld.getPlayerEntityByName(name))

            val room = getCurrentRoom()
            val time = System.currentTimeMillis() - RunInformation.startTime
            if (room != null && time > 1000) {
                if (lastRoom == null) lastRoom = room else if (lastRoom != room) {
                    roomVisits.add(Pair(time - lastTime, lastRoom!!))
                    lastTime = time
                    lastRoom = room
                }
            }
        }

        players.forEach { (_, player) ->
            MapUtils.getMapData()?.mapDecorations?.entries?.find { map -> map.key == player.icon }
                ?.let { (_, vec4b) ->
                    player.isPlayer = vec4b.func_176110_a().toInt() == 1
                    player.mapX = vec4b.func_176112_b() + 128 shr 1
                    player.mapZ = vec4b.func_176113_c() + 128 shr 1
                    player.yaw = vec4b.func_176111_d() * 22.5f
                }
            if (player.isPlayer) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX = ((mc.thePlayer.posX - STARTX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ = ((mc.thePlayer.posZ - STARTZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }

        if (RunInformation.totalPuzzles == 0) RunInformation.totalPuzzles = firstResult(Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r"), list.find { s -> s.second.contains("Puzzles:") }?.second!!)?.toIntOrNull() ?: RunInformation.totalPuzzles
    }



    fun reset() {
        dungeonList.fill(null)
        uniqueRooms.clear()
        players.clear()
        mimicPos = null
        mimicOpenTime = 0L
        hasScanned = false
        RunInformation.reset()

    }
}