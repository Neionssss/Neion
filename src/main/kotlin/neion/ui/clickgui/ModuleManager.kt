package neion.ui.clickgui

import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.ui.clickgui.settings.Setting
import neion.events.PreKeyInputEvent
import neion.events.PreMouseInputEvent
import neion.features.*
import neion.features.dungeons.*
import neion.ui.Colors
import neion.ui.Mapping
import neion.ui.Score
import neion.utils.Utils.equalsOneOf
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

/**
 * This object handles all the modules. After making a module it just has to be added to the "modules" list and
 * everything else will be taken care of automatically. This entails:
 *
 * It will be added to the click gui in the order it is put in here. But keep in mind that the category is set within
 * the module. The comments here are only for readability.
 *
 * All settings that are registered within the module will be saved to and loaded the module config.
 * For this to properly work remember to register the settings to the module.
 *
 * The module will be registered and unregistered to the forge eventbus when it is enabled / disabled.
 *
 * The module will be informed of its keybind press.
 *
 *
 * @author Aton
 * @see Module
 * @see Setting
 */
object ModuleManager {
    /**
     * All modules have to be added to this list to function!
     */
    val modules: ArrayList<Module> = arrayListOf(
        // General
        AutoSB,
        Trapper,
        JasperESP,
        ExperimentsSolver,
        HideCheapCoinTextures,
        HideGrayDamageNumbers,
        HideDeathAnimation,

        //DUNGEON
        AutoCloseChests,
        AutoGFS,
        BlazeSolver,
        Croesus,
        DungeonChestProfit,
        PreBlocks,
        GKey,
        ItemESP,
        RemoveUselessNameTags,
        TeleportMazeSolver,
        LividSolver,
        TriviaSolver,
        WeirdosSolver,
        SimonSaysSolver,
        TerminalSolvers,

        //RENDER
        ClickGui,
        CustomGUI,
        CustomScoreboard,
        CleanerTab,
        Camera,
        RemoveBlockOverlay,
        NoFire,
        NoHurtCam,
        DisableBlind,

        //misc
        CancelInteractions,
        CancelReequip,
        FreeCam,
        MurderHelper,
        NoReverse3DView,
        ToggleSprint,
        HideInventoryEffects,
        NoPushOut,
        BlockUselessMessages,
        CopyChat,

        //Map
        Mapping,
        Score,
        Colors,

        // Debug
        ForceSkyblock,
        ForcePaul
    )

    /**
     * Handles the key binds for the modules.
     * Note that the custom event fired in the minecraft mixin is used here and not the forge event.
     * That is done to run this code before the vanilla minecraft code.
     */
    @SubscribeEvent
    fun activateModuleKeyBinds(event: PreKeyInputEvent) {
        modules.filter { module -> module.hasBind && module.keyCode == event.key }.forEach { module -> module.keyBind() }
    }

    /**
     * Handles the key binds for the modules.
     * Note that the custom event fired in the minecraft mixin is used here and not the forge event.
     * That is done to run this code before the vanilla minecraft code.
     */
    @SubscribeEvent
    fun activateModuleMouseBinds(event: PreMouseInputEvent) {
        modules.filter { module -> module.hasBind && module.keyCode + 100 == event.button }.forEach { module -> module.keyBind() }
    }

    fun getModuleByName(name: String): Module? = modules.firstOrNull { it.name.equals(name, ignoreCase = true)}
}