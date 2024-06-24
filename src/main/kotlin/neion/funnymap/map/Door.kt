package neion.funnymap.map

class Door(x: Int, z: Int, var type: DoorType, var opened: Boolean = false) : Tile(x,z) {
    var nextToFairy: Boolean? = null
}