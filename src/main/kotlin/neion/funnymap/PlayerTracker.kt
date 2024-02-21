package neion.funnymap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import neion.Neion
import neion.Neion.Companion.mc
import neion.funnymap.map.*
import neion.utils.APIHandler
import neion.utils.ItemUtils.equalsOneOf
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

object PlayerTracker {

    val roomClears: MutableMap<RoomData, Set<String>> = mutableMapOf()

    fun roomStateChange(room: Tile, state: RoomState, newState: RoomState) {
        if (room !is Room) return
        if (newState.equalsOneOf(RoomState.CLEARED, RoomState.GREEN) && state != RoomState.CLEARED) {
            val currentRooms = Dungeon.dungeonTeammates.map { Pair(it.value.formattedName, it.value.getCurrentRoom()) }
            roomClears[room.data] = currentRooms.filter { it.first != "" && it.second?.core == room.data.cores[0] }.map { it.first }.toSet()
        }
    }

    fun onDungeonEnd() {
        Dungeon.dungeonTeammates.forEach { it.value.roomVisits.add(Pair(System.currentTimeMillis() - Dungeon.Info.startTime - it.value.lastTime, it.value.lastRoom)) }

        CoroutineScope(EmptyCoroutineContext).launch {
            Dungeon.dungeonTeammates.map { (_, player) ->
                async(Dispatchers.IO) { Triple(player.formattedName, player, APIHandler.getSecrets(player.uuid)) }
            }.map { ti ->
                val (name, player, secrets) = ti.await()
                    val allClearedRooms = roomClears.filter { it.value.contains(name) }
                    val soloClearedRooms = allClearedRooms.filter { it.value.size == 1 }
                    val max = allClearedRooms.size
                    val min = soloClearedRooms.size

                    val roomComponent = ChatComponentText("§b${if (soloClearedRooms.size != allClearedRooms.size) "$min-$max" else max} §3Rooms").apply {
                        chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                            player.roomVisits.groupBy { it.second }.entries.joinToString(separator = "\n", prefix = "$name's §eRoom Times:\n") { (room, times) ->
                                "§6$room §a- §b${times.sumOf { it.first }.milliseconds}"
                            }
                        )))
                    }

                   ChatComponentText("${Neion.CHAT_PREFIX} §3$name §f> ")
                        .appendSibling(ChatComponentText("§b${secrets - player.startingSecrets} §3secrets")).appendText(" §6| ")
                        .appendSibling(roomComponent).appendText(" §6 ")
            }.forEach { mc.thePlayer.addChatMessage(it) }
        }
    }
}
