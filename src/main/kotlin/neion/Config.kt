package neion

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.annotations.Number
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.libs.universal.UKeyboard
import neion.ui.EditLocationGui


object Config : Config(Mod("Neion", ModType.SKYBLOCK), "neion-config.json") {

    // General

    @Switch(
        name = "Auto join Skyblock",
    )
    var autoSB = false

    @Switch(
        name = "Hide potion effects",
        description = "Hides Inventory Potion Effects",
    )
    var hidePotionEffects = false

    @Switch(
        name = "Hide Death Animation",
    )
    var hideDeathAnimation = false

    @Switch(
        name = "Hide gray Damage numbers"
    )
    var hidegrayDamage = false

    @Switch(
        name = "Item Prices in tooltip",
    )
    var priceTooltip = false

    @Switch(
        name = "Random things",
    )
    var randomStuff = false

    @Switch(
        name = "Semi-Auto Trapper",
    )
    var trapperESP = false

    @Switch(
        name = "Remove Selfie Camera",
    )
    var noReverse3rdPerson = false

    @Switch(
        name = "No hurt cam",
    )
    var hurtCam = false


    // Keybindings
    @KeyBind(name = "gui", subcategory = "Keybindings")
    var fuck: OneKeyBind = OneKeyBind(UKeyboard.KEY_RSHIFT)

    @KeyBind(name = "pets", subcategory = "Keybindings")
    var pets: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @KeyBind(name = "trades", subcategory = "Keybindings")
    var trades: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @KeyBind(name = "equipment", subcategory = "Keybindings")
    var equipment: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @KeyBind(name = "Wardrobe", subcategory = "Keybindings")
    var wardrobe: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @KeyBind(name = "Autorun", subcategory = "Keybindings")
    var autorun: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @KeyBind(name = "Auto-sell", subcategory = "Keybindings")
    var autoSellBind = OneKeyBind(UKeyboard.KEY_NONE)


    // Dungeons
    // ------------------------------------------


    @Switch(
        name = "Auto close dungeon chests",
        category = "Dungeons",
    )
    var autoclose = false

    @Switch(
        name = "Cancel Interactions",
        category = "Dungeons",
    )
    var cancelInteractions = false

    @Switch(
        name = "Auto Enderpearl GFS",
        category = "Dungeons",
    )
    var autoGFS = false

    @Number(
        name = "Minimum Enderpearl Amount",
        category = "Dungeons",
        min = 1f,
        max = 16f
    )
    var minep = 5

    @Switch(
        name = "Extras",
        description = "Beta feature",
        category = "Dungeons"
    )
    var preBlocks = false

    @Switch(
        name = "Dungeon Chest Profit",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var chestProfit = false

    @Switch(
        name = "Chest Opener",
        description = "Not looter, yet opener",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var chestOpener = false

    @Switch(
        name = "Hide Extra Nametags",
        description = "Removes non-star mob nametags",
        category = "Dungeons",
    )
    var hideTags = false

    @Switch(
        name = "Croesus chest helper",
        description = "Hides opened chests",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var croesus = false

    @Switch(
        name = "Show Key chests",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var showKeyChests = false

    // Solvers
    // ------------------------------------------------------------------

    @Switch(
        name = "Blaze Solver",
        category = "Solvers"
    )
    var blazeSolver = false

    @Switch(
        name = "Line to next Blaze",
        category = "Solvers"
    )
    var lineToNextBlaze = false

    @Switch(
        name = "Three Weirdos Solver",
        category = "Solvers"
    )
    var threeSolver = false

    @Switch(
        name = "Teleport Maze Solver",
        category = "Solvers"
    )
    var tpMazeSolver = false

    @Switch(
        name = "Quiz Solver",
        category = "Solvers"
    )
    var quizSolver = false

    @Switch(
        name = "Auto-Weirdos",
        category = "Solvers"
    )
    var autoWeirdos = false

    @Switch(
        name = "One-buttoned Terminal",
        description = "Makes slots be in one place",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var terminalHelper = false

    @Switch(
        name = "Rubix Terminal",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var rubixSolver = false

    @Switch(
        name = "Simon Says Solver",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var ssSolver = false

    @Switch(
        name = "Starts With Solver",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var startsWithSolver = false

    @Switch(
        name = "Colors Solver",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var colorsSolver = false

    @Switch(
        name = "Numbers Terminal",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var numbersSolver = false

    @Switch(
        name = "Hide Tooltips",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var hideTooltips = false

    @Dropdown(
        name = "Prevent terminal misclicks",
        category = "Solvers",
        subcategory = "Terminals",
        options = ["OFF", "ALL", "All except melody"]
    )
    var terminalPrevent = 0

    @Color(
        name = "First Number",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var firstNumber = OneColor(0,0,200,255)

    @Color(
        name = "Second Number",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var secondNumber = OneColor(0,0,150,255)

    @Color(
        name = "Third Number",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var thirdNumber = OneColor(0,0,100,255)

    @Color(
        name = "StartsWith/Colors",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var terminalColor = OneColor(255,0,0,100)

    /* These two things are 3+ months already here
    https://github.com/inglettronald/DulkirMod
     */
    @Switch(
        name = "Turn off re-equip animation",
        category = "Other",
    )
    var cancelReequip = false

    @Switch(
        name = "Show re-equip animation when changing slots",
        description = "Will overwrite \"Turn off re-equip animation\" when switching the slot.",
        category = "Other",
    )
    var showReEquipAnimationWhenChangingSlots = true
    //

    // Other


    @Switch(
        name = "Automatic Sprint",
        category = "Other",
    )
    var ToggleSprint = false

    @Switch(
        name = "Disable Blindness",
        category = "Other"
    )
    var disableBlind = false

    @Switch(
        name = "Prevent Pushing out from Blocks",
        category = "Other",
    )
    var preventPushing = false

    @Switch(
        name = "Remove Fire F5 fire overlay",
        category = "Other"
    )
    var removeF5Fire = false

    @Switch(
        name = "Cleaner Tab",
        category = "Other"
    )
    var cleanerTab = false

    @Switch(
        name = "Murder Mystery Helper",
        category = "Other"
    )
    var murderHelper = false

    @Switch(
        name = "F5 Camera",
        category = "Other",
        subcategory = "Camera"
    )
    var f5Camera = false

    @Slider(
        name = "F5 Camera Distance",
        description = "Default is 3.5",
        min = .0f,
        max = 150.0f,
        category = "Other",
        subcategory = "Camera"
    )
    var F5CameraDistance = 3.5f

    @Switch(
        name = "Camera Clip",
        category = "Other",
        subcategory = "Camera"
    )
    var cameraClip = false

    @Switch(
        name = "Freecam",
        category = "Other",
        subcategory = "Freecam"
    )
    var freeCam = false

    @Slider(
        name = "Freespeed",
        category = "Other",
        subcategory = "Freecam",
        min = 0.8f,
        max = 5.0f
    )
    var speedVaue = 1f

    @KeyBind(name = "Freecam Keybind", category = "Other", subcategory = "Freecam")
    var freeBind: OneKeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    // GUI


    @Button(
        name = "Edit GUI Elements",
        text = "Edit GUI Elements?",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    fun editGUI() {
        Neion.display = EditLocationGui
    }

    @Switch(
        name = "Only on Skyblock",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var onlySkyblock = true

    @Switch(
        name = "Cancel Server Messages",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var cancelServerMessages = false

    @Switch(
        name = "Hide Action bar",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var hideActionbar = false

    @Switch(
        name = "Hide Hand",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var renderHand = false

    @Switch(
        name = "Hide Boss bar",
        description = "Hides most useless boss bars",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var BossBarHider = false

    @Switch(
        name = "Hide unnecessary GUI elements",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var hideGUI = false

    @Switch(
        name = "Hide Health",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var hideHealth = false

    @Switch(
        name = "Hide Experience",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var hideExperience = false

    @Switch(
        name = "Hide Scoreboard",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var hideScoreboard = false

    @Switch(
        name = "Custom Mana",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var showManaFocus = false

    @Color(
        name = "Mana Color",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var manaColor = OneColor(0, 20, 155)

    @Switch(
        name = "Custom Health",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var showHealth = false

    @Color(
        name = "Health Color",
        category = "GUI",
        subcategory = "Custom GUI"
    )
    var healthColor = OneColor(255,0,0)

    // DungeonsGUI

    @Switch(
        name = "Show Elapsed Time",
        category = "GUI",
        subcategory = "Dungeons GUI"
    )
    var showTimeFocus = false

    @Switch(
        name = "Show secrets",
        category = "GUI",
        subcategory = "Dungeons GUI"
    )
    var showSecretsFocus = false

    @Switch(
        name = "Show Cleared %",
        category = "GUI",
        subcategory = "Dungeons GUI"
    )
    var showClearedFocus = false

    @Color(
        name = "Cleared % Color",
        category = "GUI",
        subcategory = "Dungeons GUI"
    )
    var percentColor = OneColor(255, 0, 0 ,255)

    @Color(
        name = "Elapsed Time Color",
        category = "GUI",
        subcategory = "Dungeons GUI"
    )
    var timeColor = OneColor(0, 0, 255, 255)
}
