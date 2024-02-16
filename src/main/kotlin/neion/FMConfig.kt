package neion

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.annotations.Number
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.libs.universal.UKeyboard

object FMConfig : Config(Mod("NeionMap", ModType.SKYBLOCK), "nmap-config.json") {

    @Switch(
        name = "Map Enabled",
        description = "Render the map!",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapEnabled = true

    @Switch(
        name = "Hide In Boss",
        description = "Hides the map in boss.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapHideInBoss = false

    @Switch(
            name = "Show Team Info",
            description = "Shows team member secrets and room times at end of run. Requires a valid API key.",
            category = "Map",
            subcategory = "Toggle"
    )
    var teamInfo = false

    @Switch(
        name = "Scan Mimic",
        category = "Map",
        subcategory = "Toggle"
    )
    var scanMimic = true

    @Switch(
        name = "Highlight Mimic",
        description = "Draws a box at Mimic chest",
        category = "Map",
        subcategory = "Toggle"
    )
    var highLightMimic = false

    @Switch(
        name = "Mimic Room Info",
        description = "Sends a message in chat with name of the room mimic in",
        category = "Map",
        subcategory = "Toggle"
    )
    var mimicInfo = false

    @Dropdown(
        name = "Show Player Names",
        description = "Show player name under player head",
        category = "Map",
        subcategory = "Toggle",
        options = ["Off", "Holding Leap", "Always"]
    )
    var playerHeads = 0

    @Switch(
        name = "Vanilla Head Marker",
        description = "Uses the vanilla head marker for yourself.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapVanillaMarker = false

    @KeyBind(
            name = "Peek rooms",
            description = "Shows Room/Player names whenever keybind is Pressed",
            category = "Map",
            subcategory = "Toggle"
    )
    var peekBind = OneKeyBind(UKeyboard.KEY_NONE)

    @Button(
        name = "Reset Map Position",
        category = "Map",
        subcategory = "Size",
        text = "Reset"
    )
    fun resetMapLocation() {
        mapX = 10
        mapY = 10
    }

    @Number(
        name = "Map X",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 1000f
    )
    var mapX = 10

    @Number(
        name = "Map Y",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 1000f
    )
    var mapY = 10

    @Slider(
        name = "Map Size",
        description = "Scale of entire map.",
        category = "Map",
        subcategory = "Size",
        min = 0.1f,
        max = 4f
    )
    var mapScale = 1f

    @Slider(
        name = "Map Text Scale",
        description = "Scale of room names and secret counts relative to map size.",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 2f,
    )
    var textScale = 0.75f

    @Slider(
        name = "Player Heads Scale",
        description = "Scale of player heads relative to map size.",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 2f,
    )
    var playerHeadScale = 1f

    @Slider(
        name = "Player Name Scale",
        description = "Scale of player names relative to head size.",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 2f,
    )
    var playerNameScale = 0.8f

    @Color(
        name = "Map Background Color",
        category = "Map",
        subcategory = "Render",
        allowAlpha = true
    )
    var mapBackground = OneColor(0, 0, 0, 179/255)

    @Color(
        name = "Map Border Color",
        category = "Map",
        subcategory = "Render",
        allowAlpha = true
    )
    var mapBorder = OneColor(0, 0, 0, 255)

    @Slider(
        name = "Border Thickness",
        category = "Map",
        subcategory = "Render",
        min = 0f,
        max = 10f
    )
    var mapBorderWidth = 3f

    @Switch(
        name = "Dark Undiscovered Rooms",
        description = "Darkens unentered rooms.",
        category = "Rooms"
    )
    var mapDarkenUndiscovered = true

    @Dropdown(
        name = "Room Names",
        description = "Shows names of rooms on map.",
        category = "Rooms",
        options = ["None", "Puzzles / Trap", "All"]
    )
    var mapRoomNames = 1

    @Dropdown(
        name = "Room Secrets",
        description = "Shows total secrets of rooms on map.",
        category = "Rooms",
        options = ["Off", "On", "Replace Checkmark"]
    )
    var mapRoomSecrets = 0

    @Switch(
        name = "Color Text",
        description = "Colors name and secret count based on room state.",
        category = "Rooms"
    )
    var mapColorText = false

    @Dropdown(
        name = "Room Checkmarks",
        description = "Adds room checkmarks based on room state.",
        category = "Rooms",
        options = ["None", "Default", "NEU"]
    )
    var mapCheckmark = 1


    @Dropdown(
            name = "Door ESP",
            description = "Boxes unopened doors.",
            category = "Rooms",
            subcategory = "Door",
            options = ["off", "first", "all"]
    )
    var witherDoorESP = 0


    @Color(
            name = "No Key Color",
            category = "Rooms",
            subcategory = "Door",
            allowAlpha = true
    )
    var noKeyC = OneColor(255, 0, 0)

    @Color(
            name = "Has Key Color",
            category = "Rooms",
            subcategory = "Door",
            allowAlpha = true
    )
    var keyC = OneColor(0, 255, 0)

    @Slider(
            name = "Outline Width",
            category = "Rooms",
            subcategory = "Door",
            min = 1f,
            max = 10f
    )
    var witherDoorOutlineWidth = 3f

    @Slider(
        name = "Darken Multiplier",
        description = "How much to darken undiscovered rooms",
        category = "Colors",
        min = 0.0f,
        max = 1.0f
    )
    var mapDarkenPercent = 0.4f

    @Color(
        name = "Blood Door",
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorBloodDoor = OneColor(231, 0, 0)

    @Color(
        name = "Entrance Door",
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorEntranceDoor = OneColor(20, 133, 0)

    @Color(
        name = "Normal Door",
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorRoomDoor = OneColor(92, 52, 14)

    @Color(
        name = "Wither Door",
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorWitherDoor = OneColor(0, 0, 0)

    @Color(
        name = "Opened Door",
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorOpenedDoor = OneColor(92, 52, 14)

    @Color(
        name = "Blood Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorBlood = OneColor(255, 0, 0)

    @Color(
        name = "Entrance Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorEntrance = OneColor(20, 133, 0)

    @Color(
        name = "Fairy Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorFairy = OneColor(224, 0, 255)

    @Color(
        name = "Miniboss Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorMiniboss = OneColor(254, 223, 0)

    @Color(
        name = "Normal Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRoom = OneColor(107, 58, 17)

    @Color(
        name = "Mimic Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRoomMimic = OneColor(186, 66, 52)

    @Color(
        name = "Puzzle Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorPuzzle = OneColor(117, 0, 133)

    @Color(
        name = "Rare Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRare = OneColor(255, 203, 89)

    @Color(
        name = "Trap Room",
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorTrap = OneColor(216, 127, 51)

    @Switch(
        name = "Assume Spirit",
        category = "Score",
        subcategory = "Toggle"
    )
    var scoreAssumeSpirit = true

    @Switch(
        name = "Minimized Text",
        description = "Shortens description for score elements.",
        category = "Score",
        subcategory = "Toggle"
    )
    var scoreMinimizedName = false

    @Switch(
        name = "Hide in Boss",
        category = "Score",
        subcategory = "Toggle"
    )
    var scoreHideInBoss = false

    @Number(
        name = "Score Calc X",
        category = "Score",
        subcategory = "Size",
        min = 1f,
        max = 20f
    )
    var scoreX = 10

    @Number(
        name = "Score Calc Y",
        category = "Score",
        subcategory = "Size",
        min = 1f,
        max = 20f
    )
    var scoreY = 10

    @Slider(
        name = "Score Calc Size",
        description = "Scale of score calc.",
        category = "Score",
        subcategory = "Size",
        min = 0.1f,
        max = 4f,
    )
    var scoreScale = 1f

    @Switch(
        name = "Score",
        category = "Score",
        subcategory = "Elements",
    )
    var scoreTotalScore = true

    @Dropdown(
        name = "Secrets",
        category = "Score",
        subcategory = "Elements",
        options = ["Off", "Total", "Total and Missing"]
    )
    var scoreSecrets = 2

    @Switch(
        name = "Crypts",
        category = "Score",
        subcategory = "Elements"
    )
    var scoreCrypts = true

    @Switch(
        name = "Mimic",
        category = "Score",
        subcategory = "Elements"
    )
    var scoreMimic = false

    @Switch(
        name = "Deaths",
        category = "Score",
        subcategory = "Elements"
    )
    var scoreDeaths = true

    @Dropdown(
        name = "Puzzles",
        category = "Score",
        subcategory = "Elements",
        options = ["Off", "Total", "Completed and Total"]
    )
    var scorePuzzles = 0

    @Dropdown(
        name = "Score Messages",
        category = "Score",
        subcategory = "Message",
        options = ["Off", "300", "270 and 300"]
    )
    var scoreMessage = 0

    @Dropdown(
        name = "Score Title",
        description = "Shows score messages as a title notification.",
        category = "Score",
        subcategory = "Message",
        options = ["Off", "300", "270 and 300"]
    )
    var scoreTitle = 0

    @Text(
        name = "270 Message",
        category = "Score",
        subcategory = "Message"
    )
    var message270 = "270 Score"

    @Text(
        name = "300 Message",
        category = "Score",
        subcategory = "Message"
    )
    var message300 = "300 Score"

    @Switch(
        name = "300 Time",
        category = "Score",
        subcategory = "Message"
    )
    var timeTo300 = false

    @Switch(
            name = "Mimic Message",
            category = "Score",
            subcategory = "Message"
    )
    var mimicMessageEnabled = false

    @Text(
            name = "Mimic Message Text",
            category = "Score",
            subcategory = "Message"
    )
    var mimicMessage = "Mimic Killed!"

    @Dropdown(
        name = "Show Run Information",
        description = "Shows run information under map.",
        category = "Score",
        subcategory = "Toggle",
        options = ["OFF", "Default", "Separate"]
    )
    var mapShowRunInformation = 1

    @Text(
        name = "Hypixel API Key",
        category = "Debug",
    )
    var apiKey = ""

    @Switch(
        name = "Force Skyblock",
        category = "Debug"
    )
    var forceSkyblock = false

    @Switch(
        name = "Paul Score",
        description = "Assumes paul perk is active to give 10 bonus score.",
        category = "Debug"
    )
    var paulBonus = false

    fun init() {
        fun hide(option: String?) {
            if (optionNames.containsKey(option)) optionNames[option]!!.addHideCondition { true }
        }
        initialize()
        hide("scoreX")
        hide("scoreY")
        hide("scoreScale")
        hide("mapX")
        hide("mapY")
        hide("mapScale")
    }
}
