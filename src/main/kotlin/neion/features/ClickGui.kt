package neion.features

import neion.Neion.Companion.display
import neion.ui.clickgui.Category
import neion.ui.clickgui.ClickGUI
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.*
import java.awt.Color

/**
 * Settings for the CLick Gui
 * @author Aton
 */
object ClickGui: Module("ClickGUI", category = Category.RENDER, description = "Appearance settings for the click gui.", hasBind = true) {

    val blur: BooleanSetting = BooleanSetting("Blur", true, description = "Toggles the background blur for the gui.")
    val color = ColorSetting("Color", Color(134,26,71), false, description = "Color theme in the gui.")
    val chroma = BooleanSetting("Chroma")
    val chromaSpeed = NumberSetting("Chroma Speed", min = 0.1, max = 4.0, default = 1.0, increment = 0.01)
    val apiKey = StringSetting("API Key", "", length = 100)

    val panelX: MutableMap<Category, NumberSetting> = mutableMapOf()
    val panelY: MutableMap<Category, NumberSetting> = mutableMapOf()
    val panelExtended: MutableMap<Category, BooleanSetting> = mutableMapOf()

    private const val pwidth = 120.0
    private const val pheight = 15.0

    val panelWidth: NumberSetting = NumberSetting("Panel width", default = pwidth, hidden = true)
    val panelHeight: NumberSetting = NumberSetting("Panel height", default = pheight, hidden = true)

    const val advancedRelWidth = 0.5
    const val advancedRelHeight = 0.5

    val advancedRelX = NumberSetting("Advanced_RelX",(1 - advancedRelWidth)/2.0,0.0, (1- advancedRelWidth), 0.0001, hidden = true)
    val advancedRelY = NumberSetting("Advanced_RelY",(1 - advancedRelHeight)/2.0,0.0, (1- advancedRelHeight), 0.0001, hidden = true)

    init {

        addSettings(
            blur,
            color,
            chroma,
            chromaSpeed,
            apiKey,
            advancedRelX,
            advancedRelY
        )

        // The Panels

        // this will set the default click gui panel settings. These will be overwritten by the config once it is loaded
        resetPositions()

        addSettings(
            panelWidth,
            panelHeight
        )

        for (category in Category.entries) addSettings(panelX[category]!!, panelY[category]!!, panelExtended[category]!!)
    }

    /**
     * Adds if missing and sets the default click gui positions for the category panels.
     */
    fun resetPositions() {
        panelWidth.value = pwidth
        panelHeight.value = pheight

        var px = 10.0
        val py = 10.0
        val pxplus = panelWidth.value + 10
        for (category in Category.entries) {
            panelX.getOrPut(category) { NumberSetting(category.name + ",x", default = px, hidden = true) }.value = px
            panelY.getOrPut(category) { NumberSetting(category.name + ",y", default = py, hidden = true) }.value = py
            panelExtended.getOrPut(category) { BooleanSetting(category.name + ",extended", enabled = true, hidden = true) }.enabled = true
            px += pxplus
        }

        advancedRelX.reset()
        advancedRelY.reset()
    }

    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun keyBind() {
        toggle()
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        display = ClickGUI()
        super.onEnable()
        toggle()
    }

    /**
     * Override to prevent unregistering.
     * This is required for the API key to work.
     */
    override fun onDisable() {}
}