package neion.ui.clickgui

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import neion.ui.clickgui.settings.*
import java.awt.Color
import java.io.File
import java.lang.reflect.Type

class ModuleConfig(path: File) {

    class SettingSerializer : JsonSerializer<Setting> {
        override fun serialize(src: Setting?, typeOfSrc: Type?, context: JsonSerializationContext?) = JsonObject().apply {
            when (src) {
                is BooleanSetting -> addProperty(src.name, src.enabled)
                is NumberSetting -> addProperty(src.name, src.value)
                is SelectorSetting -> addProperty(src.name, src.selected)
                is StringSetting -> addProperty(src.name, src.text)
                is ColorSetting -> addProperty(src.name, src.value.rgb)
                is ActionSetting -> addProperty(src.name, "Action Setting")
            }
        }
    }

    class SettingDeserializer: JsonDeserializer<Setting> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Setting {
            if (json?.isJsonObject == true) {
                if (json.asJsonObject.entrySet().isEmpty()) return Setting("Undefined")

                /**
                 * The JsonObject for a Setting should only have one property. If more properties will be needed, this
                 * deserializer has to be updated.
                 * For now only the first element is used.
                 */
                val name = json.asJsonObject.entrySet().first().key
                val value = json.asJsonObject.entrySet().first().value

                if (value.isJsonPrimitive) return when {
                    (value as JsonPrimitive).isBoolean -> BooleanSetting(name, value.asBoolean)
                    value.isNumber -> NumberSetting(name, value.asDouble)
                    value.isString -> StringSetting(name, value.asString)
                    else -> Setting("Undefined")
                }
            }
            return Setting("Undefined")
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()


    private val configFile = File(path, "Config.json")

    init {
        if (!path.exists()) path.mkdirs()
        configFile.createNewFile()
    }


    fun loadConfig() {
        val configModules: ArrayList<Module>
        with(configFile.bufferedReader().use { it.readText() }) {
            if (this == "") return
            configModules = gson.fromJson(this, object : TypeToken<ArrayList<Module>>() {}.type)
        }
        configModules.forEach { configModule ->
            ModuleManager.getModuleByName(configModule.name)?.run {
                if (enabled != configModule.enabled) toggle()
                keyCode = configModule.keyCode
                for (setting in settings) {
                    for (configSetting in configModule.settings) {
                        // When the config parsing failed it can result in this being null. The compiler does not know this.
                        // So just ignore the warning here.
                        if (setting.name.equals(configSetting.name, ignoreCase = true)) when (setting) {
                            is BooleanSetting -> setting.enabled = (configSetting as BooleanSetting).enabled
                            is NumberSetting -> setting.value = (configSetting as NumberSetting).value
                            is ColorSetting -> setting.value = Color((configSetting as NumberSetting).value.toInt(), true)
                            is SelectorSetting -> setting.selected = (configSetting as StringSetting).text
                            is StringSetting -> setting.text = (configSetting as StringSetting).text
                        }
                    }
                }
            }

        }
    }

    fun saveConfig() {
        configFile.bufferedWriter().use { it.write(gson.toJson(ModuleManager.modules)) }
    }
}