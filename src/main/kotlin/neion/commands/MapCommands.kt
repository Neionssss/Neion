package neion.commands

import neion.MapConfig
import neion.Neion.Companion.mc
import neion.funnymap.RunInformation
import neion.funnymap.map.MapUtils.getCore
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object MapCommands : BaseCommand("nmap", listOf("nm")) {

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            MapConfig.openGui()
            return
        }
        when (args[0]) {
            "core" -> GuiScreen.setClipboardString(getCore(mc.thePlayer.posX.toInt(),mc.thePlayer.posZ.toInt()).toString())
            "rooms" -> RunInformation.onDungeonEnd()
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): MutableList<String> {
        if (args.size == 1) return getListOfStringsMatchingLastWord(args, listOf("core", "rooms"))
        return mutableListOf()
    }
}
