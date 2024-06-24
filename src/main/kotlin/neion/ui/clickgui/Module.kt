package neion.ui.clickgui

import neion.ui.clickgui.settings.Setting
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import neion.utils.TextUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge

open class Module(
    @Expose @SerializedName("name") val name: String,
    /**
     * Key code of the corresponding key bind.
     * Mouse binds will be negative: -100 + mouse button.
     * This is the same way as minecraft treats mouse binds.
     */
    @Expose @SerializedName("key") var keyCode: Int = 0,
    val category: Category = Category.MISC,
    toggled: Boolean = false,
    var hasBind: Boolean = false,
    var hidden: Boolean = false,
    @Expose @SerializedName("settings") val settings: ArrayList<Setting> = ArrayList(),
    /**
     * Will be used for an advanced info gui
     */
    var description: String = ""
){

    val mc: Minecraft by lazy { Minecraft.getMinecraft() }

    /**
     * Don't set this value directly, instead use toggle()
     */
    @Expose
    @SerializedName("enabled")
    var enabled: Boolean = toggled
        private set


    open fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }
    open fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    /**
     * Call to perform the key bind action for this module.
     * By default, this will toggle the module and send a chat message.
     * It can be overwritten in the module to change that behaviour.
     */
    open fun keyBind() {
        toggle()
        TextUtils.info("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    /**
     * Will toggle the module
     */
    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable() else onDisable()
    }

    /**
     * Adds all settings in the input to the settings field of the module.
     * This is required for saving and loading these settings to / from a file.
     * Keep in mind, that these settings are passed by reference, which will get lost if the original setting is reassigned.
     */
    fun addSettings(vararg setArray: Setting) {
        setArray.forEach(settings::add)
    }
}

enum class Category { GENERAL, DUNGEON, RENDER, MISC, MAP, DEBUG }