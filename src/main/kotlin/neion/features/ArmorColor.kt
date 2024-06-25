package neion.features

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import neion.Neion
import java.io.File

object ArmorColor {
    var armorColors = HashMap<String, Int>()
    val file = File(Neion.modDir, "armorcolors.json")
    val gson = GsonBuilder().setPrettyPrinting().create()

    fun loadConfig() {
        file.createNewFile()
        with(file.bufferedReader().use { it.readText() }) {
            if (this != "") armorColors = gson.fromJson(this, object : TypeToken<HashMap<String, Int>>() {}.type)
        }
    }

    fun saveConfig() {
        file.bufferedWriter().use {
            it.write(gson.toJson(armorColors))
        }
    }
}
