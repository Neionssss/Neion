package neion

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import org.lwjgl.input.Keyboard

object MapConfig : Config(Mod("NeionMap", ModType.SKYBLOCK), "nmap-config.json") {

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

    @Dropdown(
        name = "Show Player Names",
        description = "Show player name under player head",
        category = "Map",
        subcategory = "Toggle",
        options = ["Off", "Holding Leap", "Always"]
    )
    var playerHeads = 1

    @Switch(
        name = "Vanilla Head Marker",
        description = "Uses the vanilla head marker for yourself.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapVanillaMarker = false

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
    var playerHeadScale = 0.7f

    @Slider(
        name = "Player Name Scale",
        description = "Scale of player names relative to head size.",
        category = "Map",
        subcategory = "Size",
        min = 0f,
        max = 2f,
    )
    var playerNameScale = 0.8f

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
        options = ["None", "Puzzles / Trap"]
    )
    var mapRoomNames = 1

    @KeyBind(
        name = "Peek rooms",
        description = "Shows Room/Player names whenever keybind is Pressed",
        category = "Rooms",
    )
    var peekBind = OneKeyBind(Keyboard.KEY_NONE)

    @Switch(
        name = "Room Secrets",
        description = "Shows total secrets of rooms on map.",
        category = "Rooms",
    )
    var mapRoomSecrets = false

    @Dropdown(
        name = "Room Checkmarks",
        description = "Adds room checkmarks based on room state.",
        category = "Rooms",
        options = ["Default", "NEU", "Secrets", "Room Names"]
    )
    var mapCheckmark = 0


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


    @Slider(
        name = "Border Thickness",
        category = "Colors",
        min = 0f,
        max = 10f
    )
    var mapBorderWidth = 3f

    @Color(
        name = "Map Background Color",
        category = "Colors",
    )
    var mapBackground = OneColor(0, 0, 0, 179/255)

    @Color(
        name = "Map Border Color",
        category = "Colors",
    )
    var mapBorder = OneColor(0, 0, 0, 255)

    @Color(
        name = "Blood Door",
        category = "Colors",
        subcategory = "Doors",
    )
    var colorBloodDoor = OneColor(231, 0, 0)

    @Color(
        name = "Entrance Door",
        category = "Colors",
        subcategory = "Doors",
    )
    var colorEntranceDoor = OneColor(20, 133, 0)

    @Color(
        name = "Normal Door",
        category = "Colors",
        subcategory = "Doors",
    )
    var colorRoomDoor = OneColor(92, 52, 14)

    @Color(
        name = "Wither Door",
        category = "Colors",
        subcategory = "Doors",
    )
    var colorWitherDoor = OneColor(0, 0, 0)

    @Color(
        name = "Opened Door",
        category = "Colors",
        subcategory = "Doors",
    )
    var colorOpenedDoor = OneColor(92, 52, 14)

    @Color(
        name = "Entrance Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorEntrance = OneColor(20, 133, 0)

    @Color(
        name = "Fairy Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorFairy = OneColor(224, 0, 255)

    @Color(
        name = "Miniboss Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorMiniboss = OneColor(254, 223, 0)

    @Color(
        name = "Normal Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorRoom = OneColor(107, 58, 17)

    @Color(
        name = "Mimic Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorRoomMimic = OneColor(186, 66, 52)

    @Color(
        name = "Puzzle Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorPuzzle = OneColor(117, 0, 133)

    @Color(
        name = "Rare Room",
        category = "Colors",
        subcategory = "Rooms",
    )
    var colorRare = OneColor(255, 203, 89)

    @Color(
        name = "Trap Room",
        category = "Colors",
        subcategory = "Rooms",
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

    @Switch(
        name = "300 Time",
        category = "Score",
        subcategory = "Message"
    )
    var timeTo300 = false

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
}
