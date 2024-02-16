package neion.funnymap.map

import neion.FMConfig
import java.awt.Color

class Door(override val x: Int, override val z: Int, var type: DoorType) : Tile {
    var opened = false
    override var state = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> if (opened) FMConfig.colorOpenedDoor.toJavaColor() else FMConfig.colorBloodDoor.toJavaColor()
            DoorType.ENTRANCE -> FMConfig.colorEntranceDoor.toJavaColor()
            DoorType.WITHER -> if (opened) FMConfig.colorOpenedDoor.toJavaColor() else FMConfig.colorWitherDoor.toJavaColor()
            else -> FMConfig.colorRoomDoor.toJavaColor()
        }
    }
enum class DoorType {
    BLOOD, ENTRANCE, NORMAL, WITHER, FAIRY;

    companion object {
        fun fromMapColor(color: Int) = when (color) {
            18 -> BLOOD
            30 -> ENTRANCE
            // Champion, Fairy, Puzzle, Trap, Unopened doors render as normal doors
            74, 82, 66, 62, 85, 63 -> NORMAL
            119 -> WITHER
            else -> null
        }
    }
}
