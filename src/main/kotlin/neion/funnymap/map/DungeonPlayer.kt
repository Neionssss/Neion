package neion.funnymap.map

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import neion.funnymap.Dungeon
import neion.utils.APIHandler
import neion.utils.Location
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation
import kotlin.coroutines.EmptyCoroutineContext

data class DungeonPlayer(val skin: ResourceLocation) {

    var name = ""

    /** Minecraft formatting code for the player's name */
    var colorPrefix = 'f'

    /** The player's name with formatting code */
    val formattedName: String
        get() = "§$colorPrefix$name"

    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    /** Has information from player entity been loaded */
    var playerLoaded = false
    var icon = ""
    var renderHat = false
    var dead = false
    var uuid = ""
    var isPlayer = false

    /** Stats for compiling player tracker information */
    var startingSecrets = 0
    var lastRoom = ""
    var lastTime = 0L
    var roomVisits: MutableList<Pair<Long, String>> = mutableListOf()

    /** Set player data that requires entity to be loaded */
    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT)
        uuid = player.uniqueID.toString()
        playerLoaded = true
        CoroutineScope(EmptyCoroutineContext).launch {
            startingSecrets = APIHandler.getSecrets(uuid)
        }
    }

    fun getCurrentRoom(): Room? {
        if (dead) return null
        if (Location.inBoss) return Room(0,0, RoomData("Boss ${Location.dungeonFloor}", RoomType.BOSS, listOf(), 0, 0, 0))
        val x = (mapX - MapUtils.startCorner.first) / (MapUtils.mapRoomSize + 4)
        val z = (mapZ - MapUtils.startCorner.second) / (MapUtils.mapRoomSize + 4)
        return Dungeon.dungeonList.getOrNull(x * 2 + z * 22) as? Room ?: return null
    }
}
