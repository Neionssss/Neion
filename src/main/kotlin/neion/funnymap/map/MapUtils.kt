package neion.funnymap.map

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import neion.Neion
import neion.Neion.Companion.mc
import neion.features.dungeons.EditMode
import neion.funnymap.Dungeon
import neion.utils.Location
import neion.utils.Utils.equalsOneOf
import net.minecraft.block.BlockHopper
import net.minecraft.block.BlockStem
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.IBlockState
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.item.ItemMap
import net.minecraft.pathfinding.Path
import net.minecraft.pathfinding.PathFinder
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.WorldSettings
import net.minecraft.world.storage.MapData
import java.io.File
import kotlin.math.roundToInt

object MapUtils {

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
    fun calibrated(): Boolean {
        var start = 0
        var size = 0
        getMapData()?.colors?.forEachIndexed { index, byte ->
            if (byte.toInt() == 30) {
                if (size == 0) start = index
                size++
            } else if (size in 16..18) {
                mapRoomSize = size
                startCorner = when (Location.dungeonFloor) {
                    0 -> Pair(22, 22)
                    1 -> Pair(22, 11)
                    2, 3 -> Pair(11, 11)
                    else -> Pair(start and 127 % size, start shr 7 % size)
                }
                coordMultiplier = (size + 4.0) / Dungeon.ROOMSIZE
                return true
            }
        }
        return false
    }


    fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {

        val sortedPlayerInfo = mc.netHandler?.playerInfoMap?.sortedWith(Comparator { o1, o2 ->
            if (o1 == null) return@Comparator -1
            if (o2 == null) return@Comparator 0
            val spec = WorldSettings.GameType.SPECTATOR

            val c1 = if (o1.gameType != spec) 1 else 0
            val c2 = if (o2.gameType != spec) 1 else 0
            val c3 = (o1.playerTeam?.registeredName ?: "").compareTo(o2.playerTeam?.registeredName ?: "")

            return@Comparator when {
                c1 != c2 -> c1 - c2
                c3 != 0 -> c3
                else -> o1.gameProfile.name.compareTo(o2.gameProfile.name)
            }
        })

        return sortedPlayerInfo?.map { playerInfo -> Pair(playerInfo, mc.ingameGUI.tabList.getPlayerName(playerInfo))
        }.takeIf { it?.size!! > 18 && it[0].second.contains("§r§b§lParty §r§f(") }
    }
    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - Dungeon.STARTX) / 32f).roundToInt()
        val roomZ = ((posZ - Dungeon.STARTZ) / 32f).roundToInt()
        return Pair(roomX * 32 + Dungeon.STARTX, roomZ * 32 + Dungeon.STARTZ)
    }

    fun getRoomFromPos(pos: BlockPos) = Dungeon.dungeonList.filterIsInstance<Room>().getOrNull(((pos.x - Dungeon.STARTX + 15) shr 5) * 2 + ((pos.z - Dungeon.STARTZ + 15) shr 5) * 22)

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)
        (140 downTo 12).forEach { y ->
            val id = mc.theWorld.getChunkFromBlockCoords(BlockPos(x, 0, z)).getBlock(BlockPos(x, y, z))
            if (!id.equalsOneOf(5, 54, 146)) sb.append(id)
        }
        return sb.toString().hashCode()
    }

    fun getCurrentRoom() = Dungeon.players[mc.thePlayer?.name]?.getCurrentRoom()


    // Credit FloppaClient
    fun getRealPos(blockPos: BlockPos, roomPair: Pair<Room, Int>? = EditMode.getCurrentRoomPair()): BlockPos = getRotatedPos(blockPos, roomPair?.second!!).add(roomPair.first.x,0,roomPair.first.z)

    fun getStateIDWithRotation(iblockstate: IBlockState): IBlockState? {
        val rotation = EditMode.getCurrentRoomPair()?.second
        val horizontal = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
        val omni = PropertyDirection.create("facing")
        val down = BlockHopper.FACING
        val up = BlockStem.FACING
        val property = when {
            iblockstate.properties.contains(horizontal) -> horizontal
            iblockstate.properties.contains(omni) -> omni
            iblockstate.properties.contains(down) -> down
            iblockstate.properties.contains(up) -> up
            else -> horizontal
        }
        val facing = iblockstate.getValue(property)
        return iblockstate.withProperty(property, when (rotation) {
            90, -270 -> facing.rotateY()
            180, -180 -> facing.rotateY().rotateY()
            270, -90 -> facing.rotateY().rotateY().rotateY()
            else -> facing
        })
    }


    fun getRotatedPos(blockPos: BlockPos, rotation: Int) = when (rotation) {
        90,-270 -> BlockPos(-blockPos.z, blockPos.y, blockPos.x)
        180,-180 -> BlockPos(-blockPos.x, blockPos.y, -blockPos.z)
        270,-90 -> BlockPos(blockPos.z, blockPos.y, -blockPos.x)
        else -> blockPos
    }

    fun loadExtras() {
        file.createNewFile()
        with(file.bufferedReader().readText()) {
            if (this != "") extraRooms = gson.fromJson(this, object : TypeToken<MutableMap<String, ExtrasData>>() {}.type)
        }
    }

    fun saveExtras() = file.bufferedWriter().use { it.write(gson.toJson(extraRooms)) }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    var extraRooms: MutableMap<String, ExtrasData> = mutableMapOf()
    private val file = File(Neion.modDir, "extrasConfig.json")

    data class ExtrasData(val room: Room, val preBlocks: MutableMap<Int, MutableSet<BlockPos>> = mutableMapOf())

}
