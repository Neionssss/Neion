package neion.funnymap

import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.funnymap.map.MapUtils.mapX
import neion.funnymap.map.MapUtils.mapZ
import neion.funnymap.map.MapUtils.yaw
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.TextUtils.stripControlCodes
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import kotlin.math.roundToInt

object MapUpdate {
    fun getPlayers() {
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with((MapUtils.getDungeonTabList() ?: return)[i]) {
                val name = second.stripControlCodes().trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    // https://i.imgur.com/kzz1RwC.png
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                        colorPrefix = second.substringBefore(name, "f").last()
                        this.name = name
                        icon = "icon-$iconNum"
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return
        // Update map icons
        val time = System.currentTimeMillis() - Dungeon.Info.startTime
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.substringAfterLast("] ").split(" ")[0]
            if (name == "") continue
            Dungeon.dungeonTeammates[name]?.run {
                dead = tabText.contains("(DEAD)")
                if (dead) icon = ""
                else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
                if (!playerLoaded) mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }

                val room = getCurrentRoom()?.data?.name
                if (room != null && time > 1000) {
                    if (lastRoom == "") lastRoom = room
                    else if (lastRoom != room) {
                        roomVisits.add(Pair(time - lastTime, lastRoom))
                        lastTime = time
                        lastRoom = room
                    }
                }
            }
        }

        Dungeon.dungeonTeammates.forEach { (name, player) ->
            MapUtils.getMapData()?.mapDecorations?.entries?.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.isPlayer = vec4b.func_176110_a().toInt() == 1
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
            if (player.isPlayer || name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX = ((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ = ((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms() {
        for (x in 0..10) {
            for (z in 0..10) {
                val room = Dungeon.Info.dungeonList[z * 11 + x] ?: continue
                val mapTile = DungeonMap(MapUtils.getMapData()?.colors ?: return).scanTile(
                    x,
                    z,
                    DungeonScan.startX + x * (DungeonScan.roomSize shr 1),
                    DungeonScan.startZ + z * (DungeonScan.roomSize shr 1)) ?: continue
                if (mapTile.state.ordinal < room.state.ordinal) {
                    (room as? Room)?.let { DungeonScan.reRotate(it) }
                    PlayerTracker.roomStateChange(room, room.state, mapTile.state)
                    room.state = mapTile.state
                }
                if (room is Door && room.type.equalsOneOf(DoorType.WITHER, DoorType.BLOOD) && !room.opened && mc.theWorld.getChunkFromChunkCoords(room.x shr 4, room.z shr 4).isLoaded && mc.theWorld.getBlockState(BlockPos(room.x, 69, room.z)).block == Blocks.air) room.opened = true
            }
        }
    }
}
