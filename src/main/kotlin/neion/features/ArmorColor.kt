package neion.features

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import neion.Neion
import java.io.File

object ArmorColor {
    var armorColors = HashMap<String, Int>()
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    val file = File(Neion.modDir, "armorcolors.json")

    fun loadConfig() {
        file.createNewFile()
        with(file.bufferedReader().readText()) {
            if (this != "") armorColors = gson.fromJson(this, object : TypeToken<HashMap<String, Int>>() {}.type)
        }
    }
    fun saveConfig() = file.bufferedWriter().write(gson.toJson(armorColors))
}
