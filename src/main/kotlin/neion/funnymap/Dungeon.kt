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
import neion.utils.TextUtils.matchesAny
import net.minecraft.block.Block
import net.minecraft.event.ClickEvent
import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()
    val doors = mutableListOf<Door>()
    private val keyGainRegex = listOf(
            Regex(".+ §r§ehas obtained §r§a§r§.+Wither Key§r§e!§r"),
            Regex("§r§eA §r§a§r§.+Wither Key§r§e was picked up!§r")
    )
    private val keyUseRegex = listOf(
            Regex("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"),
            Regex("§r§a.+§r§a opened a §r§8§lWITHER §r§adoor!§r"),
    )


    // https://i.imgur.com/NutLQZQ.png
    fun getMimicRoom(): Room? {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }.groupingBy {
            ScanUtils.getRoomFromPos(it.pos)
        }.eachCount().forEach { (room, trappedChests) ->
            return Info.dungeonList.filterIsInstance<Room>().find { it == room && it.data.trappedChests < trappedChests }
        }
        return null
    }

    fun onTick() {
        if (!Location.inDungeons) return
        if (FMConfig.scanMimic && !Info.mimicFound && Location.dungeonFloor.equalsOneOf(6, 7) && getMimicRoom() != null) {
            if (FMConfig.mimicInfo) UChat.chat("&7Mimic Room: &c${getMimicRoom()?.data?.name}")
            Info.mimicFound = true
        }

        if (!MapUtils.calibrated()) MapUtils.calibrated()
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
        if (FMConfig.teamInfo) {
            if (event.packet.chatComponent.siblings.any {
                    it.chatStyle?.chatClickEvent?.run { action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats" } == true
                }) PlayerTracker.onDungeonEnd()
        }
        val form = event.packet.chatComponent.formattedText
        if (form.matchesAny(keyGainRegex)) Info.keys++
        if (form.contains(keyUseRegex[1])) Info.keys--
        if (form.matches(Regex(".+ §r§ehas obtained §r§a§r§.+Blood Key§r§e!§r"))) Info.bloodKey = true
        if (form.contains(keyUseRegex[0])) Info.bloodKey = false

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
        doors.clear()
        PlayerTracker.roomClears.clear()
        DungeonScan.hasScanned = false
        DungeonScan.rooms.clear()
        RunInformation.reset()
    }

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        val dungeonList = Array<Tile?>(121) { null }
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
            dungeonList.fill(null)
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
