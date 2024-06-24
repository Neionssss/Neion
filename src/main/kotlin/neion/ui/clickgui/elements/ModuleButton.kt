package neion.ui.clickgui.elements

import neion.Neion.Companion.mc
import neion.ui.Colors
import neion.ui.Mapping
import neion.ui.Score
import neion.ui.clickgui.ColorUtil
import neion.ui.clickgui.Module
import neion.ui.clickgui.Panel
import neion.ui.clickgui.elements.advanced.AdvancedMenu
import neion.ui.clickgui.settings.*
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import java.awt.Color

/**
 * Provides the toggle button for modules in the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
class ModuleButton(val mod: Module, var parent: Panel) {
    var menuelements: ArrayList<Element> = ArrayList()
    var x = 0
    var y = 0
    var width = 0
    var height = (mc.fontRendererObj.FONT_HEIGHT + 2)
    var extended = false

    init {
        /** Register the corresponding gui element for all non-hidden settings in the module */
        for (setting in mod.settings) {
            /** Don't show hidden settings */
            if (setting.hidden) continue

            when (setting) {
                is BooleanSetting -> menuelements.add(ElementCheckBox(this, setting))
                is NumberSetting -> menuelements.add(ElementSlider(this, setting))
                is SelectorSetting -> menuelements.add(ElementSelector (this, setting))
                is StringSetting -> menuelements.add(ElementTextField(this, setting))
                is ColorSetting -> menuelements.add(ElementColor(this, setting))
                is ActionSetting -> menuelements.add(ElementAction(this, setting))
            }
        }
        if (mod.hasBind) menuelements.add(ElementKeyBind(this, mod))
    }

    /**
     * Renders the Button
     */
    fun drawScreen(mouseX: Int, mouseY: Int) {
        if (mod.hidden) return
        if (AdvancedMenu.openMap) {
            AdvancedMenu.openMap = false
            parent.clickgui.advancedMenu = AdvancedMenu(Mapping)
            AdvancedMenu.currentOpened = Mapping
        } else if (AdvancedMenu.openScore) {
            AdvancedMenu.openScore = false
            parent.clickgui.advancedMenu = AdvancedMenu(Score)
            AdvancedMenu.currentOpened = Score
        } else if (AdvancedMenu.openColors) {
            AdvancedMenu.openColors = false
            parent.clickgui.advancedMenu = AdvancedMenu(Colors)
            AdvancedMenu.currentOpened = Colors
        }

        /** Change color on hover */
        if (isHovered(mouseX, mouseY)) Gui.drawRect(x - 2, y, x + width / 32, (y + height + 1), ColorUtil.clickGUIColor)

        /** Rendering the name in the middle */
        TextUtils.drawTotalCenteredStringWithShadow(mod.name, x + width / 2, y + 1 + height / 2, color = if (mod.enabled) ColorUtil.clickGUIColor else Color.white.rgb)
    }


    /**
	 * Handles mouse clicks for this element and returns true if an action was performed
	 */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isHovered(mouseX, mouseY)) return false
        when (mod) {
            is Colors, Score, Mapping -> {
                parent.clickgui.advancedMenu = AdvancedMenu(mod)
            }

            else -> {
                /** Toggle the mod on left click, expand its settings on right click and show an info screen on middle click */
                if (mouseButton == 0) {
                    mod.toggle()
                } else if (mouseButton == 1) {
                    /** toggle extended
                     * Disable listening for all members*/
                    if (menuelements.size > 0) {
                        extended = !extended
                            if (!extended) {
                                menuelements.forEach {
                                    if (it is ElementKeyBind) it.listening = false
                                    else if (it is ElementTextField) it.listening = false
                                }
                            }
                    }
                }
            }
        }
        AdvancedMenu.currentOpened = mod
        return true
    }

    private fun isHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
}