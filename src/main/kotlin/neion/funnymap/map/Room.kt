package neion.funnymap.map

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.utils.Location
import neion.utils.Utils.equalsOneOf
import net.minecraft.tileentity.TileEntityChest
import java.awt.Color

interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
    val color: Color
        get() = when (this) {
            is Room -> when (data.type) {
                RoomType.BLOOD -> Color(255, 0, 0)
                RoomType.CHAMPION -> MapConfig.colorMiniboss.toJavaColor()
                RoomType.ENTRANCE -> MapConfig.colorEntrance.toJavaColor()
                RoomType.FAIRY -> MapConfig.colorFairy.toJavaColor()
                RoomType.PUZZLE -> MapConfig.colorPuzzle.toJavaColor()
                RoomType.RARE -> MapConfig.colorRare.toJavaColor()
                RoomType.TRAP -> MapConfig.colorTrap.toJavaColor()
                else -> if (hasMimic() && !RunInformation.mimicKilled) MapConfig.colorRoomMimic.toJavaColor() else MapConfig.colorRoom.toJavaColor()
            }

            is Door -> when (type) {
                DoorType.BLOOD -> if (opened) MapConfig.colorOpenedDoor.toJavaColor() else MapConfig.colorBloodDoor.toJavaColor()
                DoorType.ENTRANCE -> MapConfig.colorEntranceDoor.toJavaColor()
                DoorType.WITHER -> if (opened) MapConfig.colorOpenedDoor.toJavaColor() else MapConfig.colorWitherDoor.toJavaColor()
                else -> MapConfig.colorRoomDoor.toJavaColor()
            }

            else -> MapConfig.colorRoom.toJavaColor()
        }
}
class Room(override val x: Int, override val z: Int, var data: RoomData = Dungeon.roomList.find { it.cores.contains(MapUtils.getCore(x, z)) }!!): Tile {

    // https://i.imgur.com/NutLQZQ.png
    fun hasMimic(): Boolean {
        val chestsCount = mc.theWorld.loadedTileEntityList.filterIsInstance<TileEntityChest>().filter { it.chestType == 1 }.groupingBy { MapUtils.getRoomFromPos(it.pos) }.eachCount()
        return MapConfig.scanMimic && Location.dungeonFloor.equalsOneOf(6, 7) && !hasMimic() && chestsCount.any { (room, chests) -> this == room && data.trappedChests < chests }
    }
    override var state = RoomState.UNDISCOVERED
}
data class RoomData(val name: String, val type: RoomType, val crypts: Int = 0, val secrets: Int = 0, val trappedChests: Int = 0, val cores: List<Int> = listOf())
enum class RoomType { BLOOD, CHAMPION, ENTRANCE, FAIRY, NORMAL, PUZZLE, RARE, TRAP, BOSS;

    companion object {
    fun fromColor(color: Int) = when (color) {
            18 -> BLOOD
            74 -> CHAMPION
            30 -> ENTRANCE
            82 -> FAIRY
            63 -> NORMAL
            66 -> PUZZLE
            62 -> TRAP
            else -> null
        }
    }
}
enum class Puzzle(val roomDataName: String, val tabName: String = roomDataName, var completed: Boolean = false) {
    BOMB_DEFUSE("Bomb Defuse"),
    BOULDER("Boulder"),
    CREEPER_BEAMS("Creeper Beams"),
    HIGHER_BLAZE("Higher Blaze", "Higher Or Lower"),
    ICE_FILL("Ice Fill"),
    ICE_PATH("Ice Path"),
    LOWER_BLAZE("Lower Blaze", "Higher Or Lower"),
    QUIZ("Quiz"),
    TELEPORT_MAZE("Teleport Maze"),
    THREE_WEIRDOS("Three Weirdos"),
    TIC_TAC_TOE("Tic Tac Toe"),
    WATER_BOARD("Water Board");
}
enum class RoomState { FAILED, GREEN, CLEARED, DISCOVERED, UNDISCOVERED }