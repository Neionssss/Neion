package neion.commands

import neion.FMConfig
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.funnymap.MapUpdate
import neion.funnymap.PlayerTracker
import neion.funnymap.map.ScanUtils
import neion.utils.TextUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object MapCommands : BaseCommand("nmap", listOf("nm")) {

    private val commands = listOf("help", "reset", "roomdata", "rooms")

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            FMConfig.openGui()
            return
        }
        when (args[0]) {
            "help" -> {
                TextUtils.info(
                    """
                        #§b§l<§fFunnyMap Commands§b§l>
                        #  §b/§ffunnymap §breset §9> §3Rescans the map.
                        #  §b/§ffunnymap §broomdata §9> §3Copies current room data or room core to clipboard.
                    """.trimMargin("#")
                )
            }
            // Scans the dungeon
            "reset" -> {
                Dungeon.reset()
                Dungeon.scan()
                MapUpdate.getPlayers()
                Dungeon.Info.started = true
            }

            // Copies room data or room core to clipboard
            "roomdata" -> {
                val (x,z) = ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
                val core = ScanUtils.getCore(x,z)
                val data = ScanUtils.getRoomData(core) ?: {
                    GuiScreen.setClipboardString(core.toString())
                    TextUtils.info("Existing room data not found. Copied room core to clipboard.")
                }
                    GuiScreen.setClipboardString(data.toString())
                    TextUtils.info("Copied room data to clipboard.")
            }
            "rooms" -> PlayerTracker.onDungeonEnd()
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos,
    ): MutableList<String> {
        if (args.size == 1) return getListOfStringsMatchingLastWord(args, commands)
        return mutableListOf()
    }
}
