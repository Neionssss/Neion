package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.Dungeon.fairyPos
import neion.funnymap.map.*
import neion.utils.APIHandler
import neion.utils.MapUtils
import neion.utils.TextUtils.stripControlCodes
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

object MapUpdate {

    var fairyOpened = false

    fun getPlayers() {
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with(MapUtils.getDungeonTabList()?.get(i) ?: return) {
                val name = second.stripControlCodes().trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                        icon = "icon-$iconNum"
                        hasSpirit = mc.theWorld?.playerEntities?.find { it.displayName?.unformattedText == name }?.gameProfile?.id?.let { APIHandler.getSpirit(it) } == true
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
                if (dead) icon = "" else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
                if (!renderHat) mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
            }
        }
        Dungeon.dungeonTeammates.forEach { (_, player) ->
            MapUtils.getMapData()?.mapDecorations?.entries?.find { it.key == player.icon }
                ?.let { (_, vec4b) ->
                    player.isPlayer = vec4b.func_176110_a().toInt() == 1
                    player.mapX = (vec4b.func_176112_b() + 128) shr 1
                    player.mapZ = (vec4b.func_176113_c() + 128) shr 1
                    player.yaw = vec4b.func_176111_d() * 22.5f
                }
            if (player.isPlayer) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX = ((mc.thePlayer.posX - Dungeon.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ = ((mc.thePlayer.posZ - Dungeon.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms() {
        for (pX in 0..10) {
            for (pZ in 0..10) {
                val tile = Dungeon.dungeonList[pZ * 11 + pX] ?: continue
                val mapTile = mapTile(pX, pZ) ?: continue
                (tile as? Room)?.run {
                    if (data.type == RoomType.FAIRY && state == RoomState.GREEN) fairyOpened = true
                    listOf(-15 to -15, 15 to -15, 15 to 15, -15 to 15).forEachIndexed { index, pair ->
                        val height =
                            mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
                        if (mc.theWorld.getBlockState(
                                BlockPos(
                                    x + pair.first,
                                    min(height, height - 1),
                                    z + pair.second
                                )
                            ).block == Block.getBlockById(159)
                        ) rotation = index * 90
                    }
                    if (mapTile.state.ordinal < state.ordinal) state = mapTile.state
                }
                if (tile is Door) {
                    if (tile.nextToFairy == null && !fairyOpened) {
                        fairyPos?.run {
                            tile.nextToFairy = (tile.x == first && abs(tile.z - second).shr(4) == 1) || (abs(tile.x - first).shr(4) == 1 && tile.z == second)
                        }
                    }
                    val fairyDoor = tile.nextToFairy == true && !fairyOpened
                    if ((fairyDoor || !tile.opened) && tile.type.equalsOneOf(DoorType.WITHER, DoorType.BLOOD) && mc.theWorld.getChunkFromChunkCoords(tile.x shr 4, tile.z shr 4).isLoaded) {
                        if (mc.theWorld.getBlockState(BlockPos(tile.x,69,tile.z)).block == Blocks.air) {
                            if (!fairyDoor) tile.opened = true
                        }
                    }
                }
            }
        }
    }

    fun mapTile(x: Int, z: Int): Tile? {
        val mapColors = MapUtils.getMapData()?.colors
        val centerColors = ByteArray(121)
        val sideColors = ByteArray(121)
        val halfRoom = MapUtils.mapRoomSize / 2
        val halfTile = halfRoom + 2
        val mapX = MapUtils.startCorner.first + halfRoom + x * halfTile
        val mapY = MapUtils.startCorner.second + halfRoom + z * halfTile
        val worldX = Dungeon.startX + x * (Dungeon.roomSize shr 1)
        val worldZ = Dungeon.startZ + z * (Dungeon.roomSize shr 1)
        if (mapX and mapY < 128 && mapColors != null) {
            val sideIndex = if (x % 2 == 0 && z % 2 == 0) {
                val topX = mapX - halfRoom
                val topY = mapY - halfRoom
                topY * 128 + topX
            } else if (z % 2 == 1) mapY * 128 + mapX - 4 else (mapY - 4) * 128 + mapX
            centerColors[z * 11 + x] = mapColors[mapY * 128 + mapX]
            sideColors[z * 11 + x] = mapColors[sideIndex]

            val centerColor = centerColors[z * 11 + x].toInt()
            val sideColor = sideColors[z * 11 + x].toInt()
            if (centerColor == 0) return null
            val roomType = when (sideColor) {
                18 -> RoomType.BLOOD
                74 -> RoomType.CHAMPION
                30 -> RoomType.ENTRANCE
                82 -> RoomType.FAIRY
                63 -> RoomType.NORMAL
                66 -> RoomType.PUZZLE
                62 -> RoomType.TRAP
                else -> null
            }
            val unknown = roomType?.let { RoomData("Unknown", it) }?.let { Room(worldX, worldZ, it) }
            return if (x % 2 == 0 && z % 2 == 0) {
                unknown?.apply {
                    state = when (centerColor) {
                        18 -> when (data.type) {
                            RoomType.BLOOD -> RoomState.DISCOVERED
                            RoomType.PUZZLE -> RoomState.FAILED
                            else -> state
                        }

                        30 -> when (data.type) {
                            RoomType.ENTRANCE -> RoomState.DISCOVERED
                            else -> RoomState.GREEN
                        }

                        34 -> RoomState.CLEARED
                        else -> RoomState.DISCOVERED
                    }
                }
            } else {
                if (sideColor == 0) Door(
                    worldX, worldZ, when (centerColor) {
                        18 -> DoorType.BLOOD
                        30 -> DoorType.ENTRANCE
                        119 -> DoorType.WITHER
                        // Champion, Fairy, Puzzle, Trap, Unopened doors render as normal doors
                        else -> DoorType.NORMAL
                    }).apply { if (centerColor != 85) state = RoomState.DISCOVERED } else unknown?.apply { state = RoomState.DISCOVERED }
            }
        }
        return null
    }
}