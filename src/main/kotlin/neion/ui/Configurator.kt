package neion.ui

import com.google.gson.GsonBuilder
import java.io.File

abstract class Configurator(protected val file: File) {
    protected open val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        // That one's better
        configuratorHashSet.add(this)
    }

    open fun loadConfig() {}

    open fun saveConfig() {}
    companion object {
        val configuratorHashSet = HashSet<Configurator>()
        fun loadData() {
            configuratorHashSet.forEach {
                it.file.createNewFile()
                it.loadConfig() }
        }
    }
}