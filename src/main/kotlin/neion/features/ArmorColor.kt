package neion.features

import com.google.gson.reflect.TypeToken
import neion.Neion
import neion.ui.Configurator
import java.io.File

object ArmorColor: Configurator(File(Neion.modDir, "armorcolors.json")) {
    var armorColors = HashMap<String, Int>()

    override fun loadConfig() {
        with(this.file.bufferedReader().use { it.readText() }) {
            if (this == "") return
            armorColors = gson.fromJson(this, object : TypeToken<HashMap<String, Int>>() {}.type)
        }
    }

    override fun saveConfig() {
        this.file.bufferedWriter().use {
            it.write(gson.toJson(armorColors))
        }
    }
}
