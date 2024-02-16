package neion.funnymap.map

import neion.FMConfig
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.utils.ItemUtils.equalsOneOf
import java.awt.Color

interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
    val color: Color
}
class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> FMConfig.colorBlood.toJavaColor()
            RoomType.CHAMPION -> FMConfig.colorMiniboss.toJavaColor()
            RoomType.ENTRANCE -> FMConfig.colorEntrance.toJavaColor()
            RoomType.FAIRY -> FMConfig.colorFairy.toJavaColor()
            RoomType.PUZZLE -> FMConfig.colorPuzzle.toJavaColor()
            RoomType.RARE -> FMConfig.colorRare.toJavaColor()
            RoomType.TRAP -> FMConfig.colorTrap.toJavaColor()
            else -> if (Dungeon.getMimicRoom() == this && !RunInformation.mimicKilled) FMConfig.colorRoomMimic.toJavaColor() else FMConfig.colorRoom.toJavaColor()
        }
}
data class RoomData(val name: String, val type: RoomType, val cores: List<Int>, val crypts: Int, val secrets: Int, val trappedChests: Int)
enum class RoomType { BLOOD, CHAMPION, ENTRANCE, FAIRY, NORMAL, PUZZLE, RARE, TRAP, BOSS;

    companion object {
        fun fromMapColor(color: Int): RoomType? = when (color) {
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
enum class Puzzle(val roomDataName: String, val tabName: String = roomDataName) {
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

    companion object {
        fun fromName(name: String): Puzzle? {
            return entries.find { name.equalsOneOf(it.roomDataName, it.tabName) }
        }
    }
}
enum class RoomState { FAILED, GREEN, CLEARED, DISCOVERED, UNDISCOVERED }