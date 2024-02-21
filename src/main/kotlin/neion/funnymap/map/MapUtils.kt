package neion.funnymap.map

import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.item.ItemMap
import net.minecraft.util.Vec4b
import net.minecraft.world.WorldSettings
import net.minecraft.world.storage.MapData

object MapUtils {

    val Vec4b.mapX
        get() = (this.func_176112_b() + 128) shr 1

    val Vec4b.mapZ
        get() = (this.func_176113_c() + 128) shr 1

    val Vec4b.yaw
        get() = this.func_176111_d() * 22.5f

    var startCorner = Pair(5, 5)
    var mapRoomSize = 16
    var coordMultiplier = 0.625

    fun getMapData(): MapData? {
        val map = mc.thePlayer?.inventory?.getStackInSlot(8) ?: return null
        if (map.item !is ItemMap || !map.displayName.contains("Magical Map")) return null
        return (map.item as ItemMap).getMapData(map, mc.theWorld)
    }

    /**
     * Calibrates map metrics based on the size and location of the entrance room.
     */
    fun calibrated(): Boolean {
        val (start, size) = findEntranceCorner()
        if (size.equalsOneOf(16, 18)) {
            mapRoomSize = size
            startCorner = when (Location.dungeonFloor) {
                0 -> Pair(22, 22)
                1 -> Pair(22, 11)
                2, 3 -> Pair(11, 11)
                else -> {
                    val startX = start and 127
                    val startZ = start shr 7
                    Pair(startX % (mapRoomSize + 4), startZ % (mapRoomSize + 4))
                }
            }
            coordMultiplier = (mapRoomSize + 4.0) / Dungeon.roomSize
            return true
        }
        return false
    }


    /**
     * Finds the starting index of the entrance room as well as the size of the room.
     */
    private fun findEntranceCorner(): Pair<Int, Int> {
        var start = 0
        var currLength = 0
        getMapData()?.colors?.forEachIndexed { index, byte ->
            if (byte.toInt() == 30) {
                if (currLength == 0) start = index
                currLength++
            } else {
                if (currLength >= 16) return Pair(start, currLength)
                currLength = 0
            }
        }
        return Pair(start, currLength)
    }

    private val tabListOrder = Ordering.from<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@from -1
        if (o2 == null) return@from 0
        return@from ComparisonChain.start().compareTrueFirst(o1.gameType != WorldSettings.GameType.SPECTATOR, o2.gameType != WorldSettings.GameType.SPECTATOR).compare(
            o1.playerTeam?.registeredName ?: "", o2.playerTeam?.registeredName ?: "").compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        return mc.thePlayer?.sendQueue?.playerInfoMap?.let { tabListOrder.immutableSortedCopy(it) }?.map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }?.let { if (it.size > 18 && it[0].second.contains("§r§b§lParty §r§f(")) it else null }
    }
}
