package neion.ui.clickgui

import neion.Neion
import neion.features.ClickGui
import neion.ui.clickgui.elements.ElementColor
import neion.ui.clickgui.elements.ElementSlider
import neion.ui.clickgui.elements.advanced.AdvancedMenu
import neion.utils.TextUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import java.io.IOException

/**
 * Provides and constructs the click gui menu
 * Based on HeroCode's gui.
 *
 * @author HeroCode, Aton
 */
class ClickGUI : GuiScreen() {
    var scale = 2.0

    private val logo = ResourceLocation("funnymap", "newman.png")

    /**
     * Used to add a delay for closing the gui, so that it does not instantly get closed
     */
    private var openedTime = System.currentTimeMillis()

    /**
     * Used to create the advanced menu for modules
     */
    var advancedMenu: AdvancedMenu? = null

    init { /** Create a panel for each module category */
        panels = ArrayList()
        for (category in Category.entries) {
            panels.add(object : Panel(category, this) {})
        } }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Scale the gui and the mouse coordinates
        // the handling of the mouse coordinates is not nice, since it has to be done in multiple places
        val scaledresolution = ScaledResolution(mc)
        val prevScale = mc.gameSettings.guiScale
        scale = 2.0 / scaledresolution.scaleFactor
        mc.gameSettings.guiScale = 2
        GL11.glScaled(scale, scale, scale)
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = Mouse.getX() * i1 / mc.displayWidth
        val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        /** Draw the Logo and the title */
        logo.let {
            val scaledResolution = ScaledResolution(mc)

            val logoWidth = 40
            val logoHeight = 50

            GL11.glPushMatrix()
            GL11.glTranslated(
                scaledResolution.scaledWidth.toDouble(),
                scaledResolution.scaledHeight.toDouble(),
                0.0
            )

            GL11.glScaled(2.0, 2.0, 2.0)

            GlStateManager.color(255f, 255f, 255f, 255f)
            mc.textureManager.bindTexture(it)
            Gui.drawModalRectWithCustomSizedTexture(
                - 5 - logoWidth,
                -5 - logoHeight,
                0f, 0f, logoWidth, logoHeight, logoWidth.toFloat(), logoHeight.toFloat()
            )

            TextUtils.drawString(
                "Neion",
                (-TextUtils.getStringWidth("Neion") - 5 - logoHeight / 1.5).toInt(),
                -TextUtils.fontHeight / 2 - 5 - logoHeight / 2,
                ColorUtil.clickGUIColor
            )
            GL11.glPopMatrix()
        }

        /** Calls all panels to render themselves and their module buttons and elements.
		  * Important to keep in mind: the panel rendered last will be on top.
          * For intuitive behaviour the panels have to be checked in reversed order for clicks.
          * This ensures that interactions will happen with the top panel. */
        for (p in panels) p.drawScreen(scaledMouseX, scaledMouseY, partialTicks)

        if (advancedMenu != null) advancedMenu?.drawScreen(scaledMouseX, scaledMouseY, partialTicks)

        /** Might be needed to use gui buttons */
        super.drawScreen(scaledMouseX, scaledMouseY, partialTicks)

        mc.gameSettings.guiScale = prevScale
    }


    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val mouseX = Mouse.getEventX() * super.width / super.mc.displayWidth
        val mouseY = super.height - Mouse.getEventY() * super.height / super.mc.displayHeight - 1

        //Scaling mouse coords
        val scaledMouseX = (mouseX / scale).toInt()
        val scaledMouseY = (mouseY / scale).toInt()

        var i = Mouse.getEventDWheel()
        if (i != 0) {
            if (i > 1) i = 1
            if (i < -1) i = -1
            if (isShiftKeyDown()) i *= 7
            // Scroll the advanced gui
            advancedMenu?.scroll(i, scaledMouseX, scaledMouseY)

            /** Checking all panels for scroll action.
             * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
            for (panel in panels.reversed()) {
                if (panel.scroll(i, scaledMouseX, scaledMouseY)) return
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val scaledresolution = ScaledResolution(mc)
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = Mouse.getX() * i1 / mc.displayWidth
        val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        // handle the advanced gui first
        if (advancedMenu?.mouseClicked(scaledMouseX, scaledMouseY, mouseButton) == true) return

        /** Checking all panels for click action.
          * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            // Handle panel button first.
            if (panel.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)) return

            // If no panel button was clicked check the module buttons and elements
            if (panel.extended && panel.visible) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (menuElement in moduleButton.menuelements) {
                            if (panel.shouldRender(menuElement.y + menuElement.height)) {
                                if (menuElement.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)) return
                            }
                        }
                    }
                }
            }
        }

        try {
            super.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val scaledresolution = ScaledResolution(mc)
        val i1: Int = scaledresolution.scaledWidth
        val j1: Int = scaledresolution.scaledHeight
        val k1: Int = Mouse.getX() * i1 / mc.displayWidth
        val l1: Int = j1 - Mouse.getY() * j1 / mc.displayHeight - 1
        val scaledMouseX = (k1 / scale).toInt()
        val scaledMouseY = (l1 / scale).toInt()

        // handle mouse release for advanced menu first
        advancedMenu?.mouseReleased(scaledMouseX, scaledMouseY, state)

        /** Checking all panels for mouse release action.
         * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            // Handle panel button first.
            panel.mouseReleased(state)

            // If no panel button was clicked check the module buttons and elements
            if (panel.extended && panel.visible) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (menuElement in moduleButton.menuelements) {
                            if (panel.shouldRender(menuElement.y + menuElement.height)) menuElement.mouseReleased(
                                scaledMouseX,
                                scaledMouseY,
                                state)
                        }
                    }
                }
            }
        }
        ModuleConfig(Neion.modDir).saveConfig()
        super.mouseReleased(scaledMouseX, scaledMouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {

        // First handle the advanced gui
        /** If in an advanced menu only handle that */
        if (advancedMenu != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) advancedMenu = null
            advancedMenu?.keyTyped(typedChar,keyCode)
            return
        }

        /** For key registration in the menu elements. Required for text fields.
         * Reversed order to check the panel on top first! */
        for (panel in panels.reversed()) {
            if (panel.extended && panel.visible && panel.moduleButtons.size > 0) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (menuElement in moduleButton.menuelements) {
                            if (menuElement.keyTyped(typedChar, keyCode)) return
                        }
                    }
                }
            }
        }

        /** Exits the menu when the toggle key is pressed */
        if (keyCode == ClickGui.keyCode && System.currentTimeMillis() - openedTime > 200) {
            mc.setIngameFocus()
            return
        }

        /** keyTyped in GuiScreen gets used to exit the gui on escape */
        try {
            super.keyTyped(typedChar, keyCode)
        } catch (e2: IOException) {
            e2.printStackTrace()
        }
        ModuleConfig(Neion.modDir).saveConfig()
    }

    override fun initGui() {
        openedTime = System.currentTimeMillis()
        /** Start blur */
        if (OpenGlHelper.shadersSupported && mc.renderViewEntity is EntityPlayer && ClickGui.blur.enabled) {
            mc.entityRenderer.stopUseShader()
            mc.entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
        }

        /** update panel positions to make it possible to update the positions
         * this is required for loading the panel positions from the config and for resetting the gui */
        for (panel in panels) {
            panel.x = ClickGui.panelX[panel.category]!!.value.toInt()
            panel.y = ClickGui.panelY[panel.category]!!.value.toInt()
            panel.extended = ClickGui.panelExtended[panel.category]!!.enabled
        }
    }

    override fun onGuiClosed() {
        /** End blur */
        mc.entityRenderer.stopUseShader()

        /** stop sliders from being active */
        for (panel in panels.reversed()) {
            if (panel.extended && panel.visible) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (menuElement in moduleButton.menuelements) {
                            if (menuElement is ElementSlider) menuElement.dragging = false
                            if (menuElement is ElementColor) menuElement.dragging = null
                        }
                    }
                }
            }
        }
        ModuleConfig(Neion.modDir).saveConfig()
    }

    override fun doesGuiPauseGame() = false

    companion object {
        var panels: ArrayList<Panel> = arrayListOf()
    }
}