package neion.ui.clickgui

import neion.features.ClickGui
import neion.ui.clickgui.elements.ModuleButton
import neion.utils.TextUtils
import neion.utils.TextUtils.capitalizeOnlyFirst
import net.minecraft.client.gui.Gui
import java.awt.Color

/**
 * Provides a category panel for the click gui.
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
open class Panel(var category: Category, var clickgui: ClickGUI) {
    private val title: String = category.name.capitalizeOnlyFirst()
    private var x2 = 0
    private var y2 = 0
    var dragging = false
    var visible = true
    var moduleButtons = ArrayList<ModuleButton>()

    var width: Int = ClickGui.panelWidth.value.toInt()
    var height: Int = ClickGui.panelHeight.value.toInt()
    var x: Int = ClickGui.panelX[category]!!.value.toInt()
    var y: Int = ClickGui.panelY[category]!!.value.toInt()
    var extended: Boolean = ClickGui.panelExtended[category]!!.enabled

    private var scrollOffset: Int = 0
    private val scrollAmmount: Int = 15

    init {
        for (module in ModuleManager.modules) {
            if (module.category != category) continue
            moduleButtons.add(ModuleButton(module, this))
        }
    }

    /**
	 * Rendering the element
	 */
    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }

        /** Render the module buttons and the Settings elements */
        if (extended && moduleButtons.isNotEmpty()) {
            var startY = y + height - scrollOffset
            for (moduleButton in moduleButtons) {
                // Render the module Button
                if (shouldRender(startY + moduleButton.height)) {
                    if (moduleButton.mod.enabled) Gui.drawRect(x, startY, (x + width / 32), (startY + moduleButton.height + 1), ColorUtil.clickGUIColor)
                    Gui.drawRect(
                        x,
                        startY,
                        (x + width),
                        (startY + moduleButton.height + 1),
                        Color(0, 0, 0, 40).rgb
                    )
                }
                moduleButton.x = x + 2
                moduleButton.y = startY
                moduleButton.width = width - 4
                if (shouldRender(startY + moduleButton.height)) moduleButton.drawScreen(mouseX, mouseY)

                /** Render the settings elements */
                var offs = moduleButton.height + 1
                if (moduleButton.extended && moduleButton.menuelements.isNotEmpty()) {
                    for (menuElement in moduleButton.menuelements) {
                        menuElement.offset = offs
                        menuElement.update()
                        if (shouldRender(menuElement.y + menuElement.height)) menuElement.drawScreen(
                            mouseX,
                            mouseY,
                            partialTicks
                        )
                        offs += menuElement.height
                    }
                }

                startY += offs
            }
            Gui.drawRect(x, (startY + 1), (x + width), (startY + 1), -0xe5e5e6)
        }

        // Render the Panel
        Gui.drawRect(x, y, (x + width) + 2, (y + height) + 2, ColorUtil.clickGUIColor)
        TextUtils.drawTotalCenteredStringWithShadow(title, x + width / 2, y + height / 2, -0x101011)
    }

    /**
     * Checks the given start position whether it is below the panel button.
     */
    fun shouldRender(endY: Int) = endY > y + height

    /**
     * Returns the y, from which on rendering is allowed to account for scrolling past the top.
     */
    fun validStart(startY: Int) = startY.coerceAtLeast(y)

    /**
	 * Handles interactions with the panel
	 */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            x2 = x - mouseX
            y2 = y - mouseY
            dragging = true
            return true
        } else if (mouseButton == 1 && isHovered(mouseX, mouseY)) {
            extended = !extended
            return true
        } else if (extended) {
            for (moduleButton in moduleButtons) {
                if (shouldRender(moduleButton.y + moduleButton.height)) {
                    if (moduleButton.mouseClicked(mouseX, mouseY, mouseButton)) return true
                }
            }
        }
        return false
    }

    /**
	 * Handles interactions with the panel
	 */
    fun mouseReleased(state: Int) {
        if (!visible) return
        if (state == 0) dragging = false

        // saving changes on mouse release instead of in the individual positions to have it all in one place
        ClickGui.panelX[category]!!.value = x.toDouble()
        ClickGui.panelY[category]!!.value = y.toDouble()
        ClickGui.panelExtended[category]!!.enabled = extended
    }

    /**
     * Scrolls the panel extension by the given number of elements.
     * If the panel is not extended, does nothing.
     *
     * @param amount The amount to scroll
     */
    fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        val length = isHoveredExtended(mouseX, mouseY) ?: return false
        val diff = (-amount * scrollAmmount).coerceAtMost(length - 13)

        scrollOffset = (scrollOffset + diff).coerceAtLeast(0)
        return true
    }

    /**
	 * HoverCheck
	 */
    private fun isHovered(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    /**
     * HoverCheck for extended buttons
     */
    private fun isHoveredExtended(mouseX: Int, mouseY: Int): Int? {
        if (!extended || moduleButtons.isEmpty()) return null
        var length = -scrollOffset
        for (moduleButton in moduleButtons) {
            var offs = moduleButton.height + 1
            if (moduleButton.extended && moduleButton.menuelements.isNotEmpty()) {
                for (menuElement in moduleButton.menuelements) {
                    menuElement.offset = offs
                    menuElement.update()
                    offs += menuElement.height
                }
            }
            length += offs
        }
        return if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height + length.coerceAtLeast(0)) length else null
    }
}