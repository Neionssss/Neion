package neion.funnymap.map

import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.ui.Colors
import java.awt.Color

abstract class Tile(val x: Int, val z: Int) {
    var state = RoomState.UNDISCOVERED
    val color: Color
        get() = when (this) {
            is Room -> when (data.type) {
                RoomType.BLOOD -> Colors.bloodRoom.value
                RoomType.CHAMPION -> Colors.miniBossRoom.value
                RoomType.ENTRANCE -> Colors.entranceRoom.value
                RoomType.FAIRY -> Colors.fairyRoom.value
                RoomType.PUZZLE -> Colors.puzzleRoom.value
                RoomType.RARE -> Colors.rareRoom.value
                RoomType.TRAP -> Colors.trapRoom.value
                else -> if (Dungeon.getMimicRoom() == this && !RunInformation.mimicKilled) Colors.mimicRoom.value else Colors.normalRoom.value
            }

            is Door -> when (type) {
                DoorType.BLOOD -> if (opened) Colors.openedDoor.value else Colors.bloodDoor.value
                DoorType.ENTRANCE -> Colors.entranceDoor.value
                DoorType.WITHER -> if (opened) Colors.openedDoor.value else Colors.witherDoor.value
                else -> Colors.normalDoor.value
            }

            else -> Colors.normalRoom.value
        }
}