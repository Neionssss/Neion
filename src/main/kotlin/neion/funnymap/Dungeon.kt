package neion.funnymap

import cc.polyfrost.oneconfig.libs.universal.UChat
import neion.Config
import neion.FMConfig
import neion.Neion.Companion.mc
import neion.events.ChatEvent
import neion.features.dungeons.EditMode
import neion.funnymap.map.*
import neion.utils.ItemUtils.equalsOneOf
import neion.utils.Location
import neion.utils.RenderUtil
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.matchesAny
import net.minecraft.block.Block
import net.minecraft.event.ClickEvent
import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()
    val espDoors = mutableListOf<Door>()
    private val keyNames = listOf(
        Regex(".+ §r§ehas obtained §r§a§r§Wither Key§r§e!§r"),
        Regex("§r§eA §r§a§r§Wither Key§r§e was picked up!§r")
    )



    // https://i.imgur.com/NutLQZQ.png
    fun getMimicRoom(): Room? {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }.groupingBy {
            ScanUtils.getRoomFromPos(
                it.pos)
        }.eachCount().forEach { (room, trappedChests) ->
            Info.dungeonList.filterIsInstance<Room>()
                .find { it == room && it.data.trappedChests < trappedChests }?.let {
                    return it
                }
        }
        return null
    }

    fun onTick() {
        if (!Location.inDungeons) return

        if (shouldSearchMimic() && getMimicRoom() != null) {
            if (FMConfig.mimicInfo) UChat.chat("&7Mimic Room: &c${getMimicRoom()?.data?.name}")
            Info.mimicFound = true
        }

        if (!MapUtils.calibrateMap()) MapUtils.calibrateMap()
        MapUpdate.updateRooms()
        RunInformation.checkMimicDead()
        ScoreCalculation.updateScore()

        MapUtils.getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updatePuzzleCount(it)
        }
        if (DungeonScan.shouldScan) DungeonScan.scan()
        if (Config.preBlocks) {
            (EditMode.getCurrentRoomPair() ?: return).run roomPair@{
                ScanUtils.extraRooms[this.first.data.name]?.run {
                    this.preBlocks.forEach { (blockID, posList) ->
                        posList.forEach {
                            mc.theWorld.setBlockState(
                                ScanUtils.getRealPos(it, this@roomPair),
                                ScanUtils.getStateFromIDWithRotation(Block.getStateById(blockID), this@roomPair.second))
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(e: RenderWorldLastEvent) {
        if (!FMConfig.highLightMimic || !Location.inDungeons) return
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 && ScanUtils.getRoomFromPos(it.pos) == getMimicRoom() }.forEach {
            if (getMimicRoom() == EditMode.getCurrentRoomPair()?.first) RenderUtil.drawBlockBox(it.pos, Color.blue, outline = true, fill = false, esp = true)
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (!Location.inDungeons || event.packet.type == 2.toByte()) return
        val form = event.packet.chatComponent.formattedText
        if (!FMConfig.teamInfo) {
            if (event.packet.chatComponent.siblings.any {
                    it.chatStyle?.chatClickEvent?.run { action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats" } == true
                }) PlayerTracker.onDungeonEnd()
        }
        if (keyNames.any { it.matches(form) }) Info.keys++
        if (form.contains(Regex("§r§a.+§r§a opened a §8§lWITHER §r§adoor!"))) Info.keys--
        if (form.contains(Regex(".+ §r§ehas obtained Blood Key!"))) Info.bloodKey = true
        if (form.contains("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r")) Info.bloodKey = false

        when (event.text) {
            "Starting in 4 seconds." -> for (i in listOf(5, 9, 13, 17, 1)) (MapUtils.getDungeonTabList() ?: return)[i].first.locationSkin
            "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.getPlayers()
                Info.startTime = System.currentTimeMillis()
                Info.started = true
            }

            "[BOSS] The Watcher: You have proven yourself. You may pass." -> RunInformation.bloodDone = true
        }
    }



    fun reset() {
        Info.reset()
        dungeonTeammates.clear()
        espDoors.clear()
        PlayerTracker.roomClears.clear()
        DungeonScan.hasScanned = false
        DungeonScan.rooms.clear()
        RunInformation.reset()
    }

    private fun shouldSearchMimic() = FMConfig.scanMimic &&
            !Info.mimicFound &&
            Location.dungeonFloor.equalsOneOf(6, 7)

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        val dungeonList = Array<Tile?>(121) { Unknown(0, 0) }
        val uniqueRooms = mutableListOf<Pair<Room, Pair<Int, Int>>>()
        var roomCount = 0
        val puzzles = mutableMapOf<Puzzle, Boolean>()

        var trapType = ""
        var cryptCount = 0
        var secretCount = 0
        var mimicFound = false
        var started = false

        var startTime = 0L
        var keys = 0
        var bloodKey = false
        fun reset() {
            dungeonList.fill(Unknown(0, 0))
            roomCount = 0
            uniqueRooms.clear()
            puzzles.clear()

            trapType = ""
            cryptCount = 0
            secretCount = 0
            mimicFound = false
            started = false
            bloodKey = false

            startTime = 0L
            keys = 0
        }
    }
}