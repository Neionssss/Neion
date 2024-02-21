package neion.funnymap.map

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import neion.Neion
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.utils.ItemUtils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.block.BlockHopper
import net.minecraft.block.BlockStem
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import java.io.File
import java.lang.reflect.Type
import kotlin.math.roundToInt

object ScanUtils {
    private val FACING_HORIZONTAL = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
    private val FACING_OMNI = PropertyDirection.create("facing")
    private val FACING_UP = BlockStem.FACING
    private val FACING_DOWN = BlockHopper.FACING
    private val roomList: Set<RoomData> = Gson().fromJson(mc.resourceManager.getResource(ResourceLocation("funnymap", "rooms.json")).inputStream.bufferedReader(), object : TypeToken<Set<RoomData>>() {}.type)


    fun getRoomData(hash: Int): RoomData? {
        return roomList.find { it.cores.contains(hash) } // Bitcoin Mining //
    }

    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - Dungeon.startX) / 32f).roundToInt()
        val roomZ = ((posZ - Dungeon.startZ) / 32f).roundToInt()
        return Pair(roomX * 32 + Dungeon.startX, roomZ * 32 + Dungeon.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - Dungeon.startX + 15) shr 5)
        val z = ((pos.z - Dungeon.startZ + 15) shr 5)
        val room = Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22)
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


    // Credit FloppaClient
    fun getRealPos(blockPos: BlockPos, roomPair: Pair<Room, Int>): BlockPos {
        return getRotatedPos(blockPos, roomPair.second).add(roomPair.first.x,0,roomPair.first.z)
    }

    fun getStateFromIDWithRotation(iblockstate: IBlockState, rotation: Int) : IBlockState {
        fun getRotatedFacing(facing: EnumFacing, rotation: Int): EnumFacing {
            return when {
                rotation.equalsOneOf(90, -270) -> facing.rotateY()
                rotation.equalsOneOf(180, -180) -> facing.rotateY().rotateY()
                rotation.equalsOneOf(270, -90) -> facing.rotateY().rotateY().rotateY()
                else -> facing
            }
        }
        var blockstate = iblockstate
        // rotate if block has rotation data. this is really scuffed unfortunately
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

    fun getRotatedPos(blockPos: BlockPos, rotation: Int): BlockPos {
        return when {
            rotation.equalsOneOf(90, -270) -> BlockPos(-blockPos.z, blockPos.y, blockPos.x)
            rotation.equalsOneOf(180, -180) -> BlockPos(-blockPos.x, blockPos.y, -blockPos.z)
            rotation.equalsOneOf(270, -90) -> BlockPos(blockPos.z, blockPos.y, -blockPos.x)
            else -> blockPos
        }
    }

    fun loadExtras() {
        file.createNewFile()
        with(file.bufferedReader().use { it.readText() }) {
            if (this == "") return
            extraRooms = gson.fromJson(this, object : TypeToken<MutableMap<String, ExtrasData>>() {}.type)
        }
    }

    fun saveExtras() {
        file.bufferedWriter().use { it.write(gson.toJson(extraRooms)) }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<MutableSet<BlockPos>>() {}.type, SetBlockPosSerializer())
        .registerTypeAdapter(object : TypeToken<MutableSet<BlockPos>>() {}.type, SetBlockPosDeserializer())
        .setPrettyPrinting().create()

    var extraRooms: MutableMap<String, ExtrasData> = mutableMapOf()
    private val file = File(Neion.modDir, "extrasConfig.json")

    data class ExtrasData(val baseCore: Int, val preBlocks: MutableMap<Int, MutableSet<BlockPos>> = mutableMapOf())

    class SetBlockPosDeserializer : JsonDeserializer<MutableSet<BlockPos>> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MutableSet<BlockPos> {
            val blockSet = mutableSetOf<BlockPos>()

            if (json?.isJsonArray!!) {
                json.asJsonArray.forEach { element ->
                    // drop first and last element as those are "
                    val coordList = (element.toString().dropLast(1).drop(1).takeIf(String::isNotEmpty)?.split(", ") ?: listOf()).map { it.toIntOrNull() ?: 0 }
                    if(coordList.size >= 3) blockSet.add(BlockPos(coordList[0], coordList[1], coordList[2]))
                }
            }
            return blockSet
        }
    }
    class SetBlockPosSerializer : JsonSerializer<MutableSet<BlockPos>> {
        override fun serialize(src: MutableSet<BlockPos>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val jsonArray = JsonArray()
            src?.forEach { jsonArray.add(JsonPrimitive("${it.x}, ${it.y}, ${it.z}")) }
            return jsonArray
        }
    }
}
