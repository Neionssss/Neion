package neion.utils

import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.funnymap.map.Room
import neion.funnymap.map.RoomData
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.block.BlockHopper
import net.minecraft.block.BlockStem
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.IBlockState
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.item.ItemMap
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.WorldSettings
import net.minecraft.world.storage.MapData
import kotlin.math.roundToInt

object MapUtils {

    private val FACING_HORIZONTAL = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
    private val FACING_OMNI = PropertyDirection.create("facing")
    private val FACING_UP = BlockStem.FACING
    private val FACING_DOWN = BlockHopper.FACING
    private val roomList: Set<RoomData> = Gson().fromJson(mc.resourceManager.getResource(ResourceLocation("funnymap", "rooms.json")).inputStream.bufferedReader(), object : TypeToken<Set<RoomData>>() {}.type)

    var startCorner = Pair(5, 5)
    var mapRoomSize = 16
    var coordMultiplier = 0.625

    fun getMapData(): MapData? {
        val map = mc.thePlayer?.inventory?.getStackInSlot(8) ?: return null
        if (map.item !is ItemMap || !map.displayName.contains("Magical Map")) return null
        return (map.item as ItemMap).getMapData(map, mc.theWorld)
    }
    /**
     * Calibrates map metrics based on the size and location of the entrance room.
     */
    fun calibrateMap(): Boolean {
        val (start, size) = findEntranceCorner()
        if (size.equalsOneOf(16, 18)) {
            mapRoomSize = size
            startCorner = when (Location.dungeonFloor) {
                0 -> Pair(22, 22)
                1 -> Pair(22, 11)
                2, 3 -> Pair(11, 11)
                else -> {
                    val startX = start and 127
                    val startZ = start shr 7
                    Pair(startX % (mapRoomSize + 4), startZ % (mapRoomSize + 4))
                }
            }
            coordMultiplier = (mapRoomSize + 4.0) / Dungeon.roomSize
            return true
        }
        return false
    }

    /**
     * Finds the starting index of the entrance room as well as the size of the room.
     */
    private fun findEntranceCorner(): Pair<Int, Int> {
        var start = 0
        var currLength = 0
        getMapData()?.colors?.forEachIndexed { index, byte ->
            if (byte.toInt() == 30) {
                if (currLength == 0) start = index
                currLength++
            } else {
                if (currLength >= 16) {
                    return Pair(start, currLength)
                }
                currLength = 0
            }
        }
        return Pair(start, currLength)
    }

    private val tabListOrder = Ordering.from<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@from -1
        if (o2 == null) return@from 0
        return@from ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR,
            o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "", o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    fun getDungeonTabList() = tabListOrder?.immutableSortedCopy(mc.netHandler?.playerInfoMap!!)?.map {
        Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }?.let {
        if (it.size > 18 && it[0].second.contains("§r§b§lParty §r§f(")) it else null
    }

    fun getRoomData(hash: Int): RoomData? = roomList.find { it.cores.contains(hash) } // Bitcoin Mining //

    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - Dungeon.startX) / 32f).roundToInt()
        val roomZ = ((posZ - Dungeon.startZ) / 32f).roundToInt()
        return Pair(roomX * 32 + Dungeon.startX, roomZ * 32 + Dungeon.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - Dungeon.startX + 15) shr 5)
        val z = ((pos.z - Dungeon.startZ + 15) shr 5)
        val room = Dungeon.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)
        for (y in 140 downTo 12) {
            val id = Block.blockRegistry.getIDForObject(mc.theWorld.getChunkFromBlockCoords(BlockPos(x, 0, z)).getBlock(BlockPos(x, y, z)))
            if (!id.equalsOneOf(5, 54, 146)) sb.append(id)
        }
        return sb.toString().hashCode()
    }

    fun getRelativePos(blockPos: BlockPos, roomPair: Pair<Room, Int>) = getRotatedPos(blockPos.add(-roomPair.first.x, 0, -roomPair.first.z), -roomPair.second)

    // Everything lower is from FloppaClient, thanks
    fun getRealPos(blockPos: BlockPos, roomPair: Pair<Room, Int>): BlockPos = getRotatedPos(blockPos, roomPair.second).add(roomPair.first.x,0,roomPair.first.z)

    fun getStateFromIDWithRotation(iblockstate: IBlockState, rotation: Int) : IBlockState {
        var blockstate = iblockstate
        if (blockstate.properties.containsKey(FACING_HORIZONTAL)) {
            val facing = blockstate.getValue(FACING_HORIZONTAL)
            if (facing.axis.isHorizontal) blockstate = blockstate.withProperty(FACING_HORIZONTAL, getRotatedFacing(facing, rotation))
        } else if (blockstate.properties.containsKey(FACING_OMNI)) {
            val facing = blockstate.getValue(FACING_OMNI)
            if (facing.axis.isHorizontal) blockstate = blockstate.withProperty(FACING_OMNI, getRotatedFacing(facing, rotation))
        } else if (blockstate.properties.containsKey(FACING_DOWN)) {
            val facing = blockstate.getValue(FACING_DOWN)
            if (facing.axis.isHorizontal) blockstate = blockstate.withProperty(FACING_DOWN, getRotatedFacing(facing, rotation))
        } else if (blockstate.properties.containsKey(FACING_UP)) {
            val facing = blockstate.getValue(FACING_UP)
            if (facing.axis.isHorizontal) blockstate = blockstate.withProperty(FACING_UP, getRotatedFacing(facing, rotation))
        }
        return blockstate
    }

    fun getRotatedFacing(facing: EnumFacing, rotation: Int): EnumFacing = when (rotation) {
        90, -270 -> facing.rotateY()
        180, -180 -> facing.rotateY().rotateY()
        270, -90 -> facing.rotateY().rotateY().rotateY()
        else -> facing
    }

    fun getRotatedPos(blockPos: BlockPos, rotation: Int) = when (rotation) {
        90, -270 -> BlockPos(-blockPos.z, blockPos.y, blockPos.x)
        180, -180 -> BlockPos(-blockPos.x, blockPos.y, -blockPos.z)
        270, -90 -> BlockPos(blockPos.z, blockPos.y, -blockPos.x)
        else -> blockPos
    }
}
