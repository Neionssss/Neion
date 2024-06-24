package neion.funnymap.map

import neion.Neion.Companion.mc
import neion.utils.MapUtils
import net.minecraft.tileentity.TileEntityChest

class Room(x: Int, z: Int, var data: RoomData, var core: Int = 0) : Tile(x,z) {
    var rotation: Int = 0
    val hasMimic: Boolean
        get() {
            mc.theWorld?.loadedTileEntityList?.filter { it is TileEntityChest && it.chestType == 1 }?.groupingBy {
                MapUtils.getRoomFromPos(it.pos)
            }?.eachCount()?.forEach { (room, trappedChests) ->
                return this == room && data.trappedChests < trappedChests
            }
            return false
        }
}