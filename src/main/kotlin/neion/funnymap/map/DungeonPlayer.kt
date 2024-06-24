package neion.funnymap.map

import neion.funnymap.Dungeon
import neion.utils.Location
import neion.utils.MapUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation

data class DungeonPlayer(val skin: ResourceLocation) {

    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    /** Has information from player entity been loaded */
    var icon = ""
    var renderHat = false
    var dead = false
    var isPlayer = false
    var hasSpirit = false

    /** Set player data that requires entity to be loaded */
    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT) == true
    }

    fun getCurrentRoom(): Room? {
        val x = (mapX - MapUtils.startCorner.first) / (MapUtils.mapRoomSize + 4)
        val z = (mapZ - MapUtils.startCorner.second) / (MapUtils.mapRoomSize + 4)
        return if (dead) null else
        if (Location.inBoss) Room(0,0, RoomData("Boss ${Location.dungeonFloor}", RoomType.BOSS)) else Dungeon.dungeonList.getOrNull(x * 2 + z * 22) as? Room
    }
    fun getCurrentRoomPair(): Pair<Room, Int>? {
        val room = getCurrentRoom() ?: return null
        return if (room.data.type == RoomType.BOSS) Pair(room, 0) else Pair(room, room.rotation)
    }
}
