package neion.funnymap.map

class Room(x: Int, z: Int, var data: RoomData, var core: Int = 0, var rotation: Int? = null) : Tile(x,z) {
    var hasMimic: Boolean = false
}