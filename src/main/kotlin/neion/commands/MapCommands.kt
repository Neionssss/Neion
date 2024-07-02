package neion.commands

import neion.Neion.Companion.display
import neion.Neion.Companion.mc
import neion.funnymap.Dungeon
import neion.funnymap.MapUpdate
import neion.funnymap.RunInformation
import neion.ui.clickgui.ClickGUI
import neion.ui.clickgui.elements.advanced.AdvancedMenu
import neion.utils.MapUtils
import neion.utils.TextUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object MapCommands : BaseCommand("nmap", listOf("nm")) {

    private val commands = listOf("help", "reset", "roomdata")

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            display = ClickGUI()
            AdvancedMenu.openMap = true
            return
        }
        when (args[0]) {
            "help" -> {
                TextUtils.info(
                    """
                        #§b§l<§fMap Commands§b§l>
                        #  §b/§fnm §breset §9> §3Rescans the map.
                        #  §b/§fnm §broomdata §9> §3Copies current room data or room core to clipboard.
                    """.trimMargin("#")
                )
            }
            // Scans the dungeon
            "reset" -> {
                Dungeon.reset()
                Dungeon.scan()
                MapUpdate.getPlayers()
                RunInformation.started = true
            }

            // Copies room data or room core to clipboard
            "core" -> {
                val (x, z) = MapUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
                GuiScreen.setClipboardString(MapUtils.getCore(x, z).toString())
            }
            "roomdata" -> {
                val (x, z) = MapUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
                val core = MapUtils.getCore(x, z)
                val data = MapUtils.getRoomData(core)
                if (data == null) {
                GuiScreen.setClipboardString(core.toString())
                TextUtils.info("Existing room data not found. Copied room core to clipboard.")
                } else {
                    GuiScreen.setClipboardString(data.toString())
                    TextUtils.info("Copied room data to clipboard.")
                }
            }
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
