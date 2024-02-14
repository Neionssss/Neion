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
        name = "Hide Enchant rune effects",
        description = "Yoy",
    )
    var HideEnchantRune = false

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
        description = "Get rid of pesky reverse third person!",
    )
    var noReverse3rdPerson = false

    @Switch(
        name = "No hurt cam",
    )
    var hurtCamIntensity = false


    @Switch(
            name = "Jasper Scanner",
            subcategory = "Gemstone"
    )
    var JasperESP = false

    @Switch(
            name = "Chest ESP",
            subcategory = "Gemstone"
    )
    var chestESP = false

    @Slider(
            name = "Scan Range",
            description = "Range for scanning (blocks)",
            subcategory = "Gemstone",
            min = 512F,
            max = 1024F
    )
    var JasperESPRange = 512

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
        name = "Fake Haste",
        description = "No silverfish required!",
        category = "Dungeons"
    )
    var fakeHaste = false

    //

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
        name = "Hide Heart Particles",
        description = "Useful for hyperion and healer bullshit",
        category = "Dungeons",
    )
    var hideHeartParticles = false

    @Switch(
        name = "Hide Extra Nametags",
        description = "Removes non-star mob nametags",
        category = "Dungeons",
    )
    var hideTags = false


    @Switch(
        name = "Croesus Hider",
        description = "You don't want to see those chests! I knew it",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var croesus = false

    @Switch(
        name = "Show Dungeon Key chests",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var showKeyChests = false

    @Switch(
        name = "Auto Croesus",
        description = "I don't think you should trust him",
        category = "Dungeons",
        subcategory = "Chest Profit"
    )
    var autoCroesus = false

    @Slider(
        name = "Auto Croesus Delay",
        category = "Dungeons",
        subcategory = "Chest Profit",
        min = 300f,
        max = 1500f
    )
    var autoCroesusDelay = 1000

    @Switch(
        name = "Ghost Block",
        description = "GhOsTbLoCk",
        category = "Dungeons",
        subcategory = "GKey"
    )
    var GGkey = false

    @Switch(
        name = "Right Click Pick GB",
        category = "Dungeons",
        subcategory = "GKey"
    )
    var rcmGB = false

    @KeyBind(
        name = "GKey Keybind",
        category = "Dungeons",
        subcategory = "GKey"
    )
    var GGkeyBind = OneKeyBind(UKeyboard.KEY_NONE)

    @Slider(
        name = "GKey Delay",
        category = "Dungeons",
        subcategory = "GKey",
        min = 0f,
        max = 500f
    )
    var GDelay = 50f

    @Slider(
        name = "GKey Range",
        category = "Dungeons",
        subcategory = "GKey",
        min = 5f,
        max = 100f,
        step = 0
    )
    var GRange = 5f

    // DungeonESP

    @Switch(
        name = "Dropped Items ESP",
        category = "Dungeons",
        subcategory = "ESP"
    )
    var itemESP = false

    @Color(
        name = "Item Color",
        category = "Dungeons",
        subcategory = "ESP"
    )
    var itemColor = OneColor(10, 15, 50)

    @Slider(
        name = "ESP outline width",
        category = "Dungeons",
        subcategory = "ESP",
        min = 1F,
        max = 10F
    )
    var espOutlineWidth = 1F

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

    @Number(
        name = "Blazes Aligned",
        category = "Solvers",
        min = 2F,
        max = 10F
    )
    var blazeLines = 2

    @Switch(
        name = "Three Weirdos Solver",
        category = "Solvers"
    )
    var threeSolver = false

    @Switch(
        name = "Quiz Solver",
        category = "Solvers"
    )
    var quizSolver = false

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

    @Switch(
        name = "Middle Click Terminals",
        category = "Solvers",
        subcategory = "Terminals"
    )
    var middleClickTerms = false

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
        name = "Funny Items",
        category = "Other"
    )
    var funnyItems = false

    @Slider(
        name = "Item swing speed",
        category = "Other",
        min = -2f,
        max = 1f
    )
    var itemSwingSpeed = 0f

    @Switch(
        name = "Prevent Pushing out from Blocks",
        category = "Other",
    )
    var preventPushing = false

    @Switch(
        name = "Remove Fire F3 fire overlay",
        category = "Other"
    )
    var removeF3Fire = false

    @Text(
        name = "Minecraft Title",
        category = "Other"
    )
    var mcTitle = "Minecraft 1.8.9"

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
        name = "Player ESP",
        category = "Other",
        subcategory = "ESP"
    )
    var playerESP = false

    @Switch(
        name = "Hide own name",
        category = "Other",
        subcategory = "ESP"
    )
    var showOwnName = false

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
        Neion.display = EditLocationGui()
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
    var guiHideFocus = false

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

    @Number(
        name = "X Value",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var x = 10

    @Number(
        name = "Y Value",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var y = 10

    @Number(
        name = "Scaler",
        category = "GUI",
        min = 0.1f,
        max = 4f
    )
    var scale = 1f

    //
    @Number(
        name = "Secret X",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var xY = 500

    @Number(
        name = "Secret Y",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var yY = 470

    @Number(
        name = "Secret Scale",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var secretsScale = 1f

    //
    @Number(
        name = "Cleared X",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var clearedX = 840

    @Number(
        name = "Cleared Y",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var clearedY = 470

    @Number(
        name = "Cleared Scale",
        category = "GUI",
        min = 0.1f,
        max = 1000f
    )
    var clearedScale = 1f

    @Number(
        name = "Time X",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var timeX = 840

    @Number(
        name = "Time Y",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var timeY = 200

    @Number(
        name = "Time Scale",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var timeScale = 1f

    @Number(
        name = "Mana X",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var manaX = 500

    @Number(
        name = "Mana Y",
        category = "GUI",
        min = 0f,
        max = 1000f
    )
    var manaY = 500

    @Number(
        name = "Mana Scale",
        category = "GUI",
        min = 0.1f,
        max = 4f
    )
    var manaScale = 1f

    fun init() {
        fun hide(option: String?) {
            if (optionNames.containsKey(option)) optionNames[option]!!.addHideCondition { true }
        }
        initialize()
        addDependency("autoCroesus", "croesus")
        addDependency("showKeyChests", "croesus")
        addDependency("blazeLines", "lineToNextBlaze")
        addDependency("blazeLines", "blazeSolver")
        addDependency("lineToNextBlaze", "blazeSolver")
        hide("manaScale")
        hide("manaY")
        hide("manaX")
        hide("timeScale")
        hide("timeY")
        hide("timeX")
        hide("clearedScale")
        hide("clearedY")
        hide("clearedX")
        hide("secretsScale")
        hide("yY")
        hide("xY")
        hide("scale")
        hide("x")
        hide("y")
    }
}

