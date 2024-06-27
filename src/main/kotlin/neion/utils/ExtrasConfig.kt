package neion.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import neion.Neion
import net.minecraft.util.BlockPos
import java.io.File
import java.lang.reflect.Type

object ExtrasConfig {

    fun loadExtras() {
        file.createNewFile()
        with(file.bufferedReader().use { it.readText() }) {
            if (this == "") return
            extraRooms = gson.fromJson(this, object : TypeToken<MutableMap<String, ExtrasData>>() {}.type)
        }
    }

    fun saveExtras() = file.bufferedWriter().use { it.write(gson.toJson(extraRooms)) }

    val gson = GsonBuilder().registerTypeAdapter(object : TypeToken<MutableSet<BlockPos>>() {}.type, SetBlockPosSerializer()).registerTypeAdapter(object : TypeToken<MutableSet<BlockPos>>() {}.type, SetBlockPosDeserializer()).setPrettyPrinting().create()

    var extraRooms: MutableMap<String, ExtrasData> = mutableMapOf()
    val file = File(Neion.modDir, "extrasConfig.json")

    data class ExtrasData(val baseCore: Int, val preBlocks: MutableMap<Int, MutableSet<BlockPos>> = mutableMapOf())

    class SetBlockPosDeserializer : JsonDeserializer<MutableSet<BlockPos>> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MutableSet<BlockPos> {
            val blockSet = mutableSetOf<BlockPos>()

            if (json?.isJsonArray!!)json.asJsonArray.forEach { element ->
                // drop first and last element as those are "
                val coordList = (element.toString().dropLast(1).drop(1).takeIf(String::isNotEmpty)?.split(", ") ?: listOf()).map { it.toIntOrNull() ?: 0 }
                if (coordList.size >= 3) blockSet.add(BlockPos(coordList[0], coordList[1], coordList[2]))
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