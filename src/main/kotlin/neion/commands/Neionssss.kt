package neion.commands

import neion.Config
import neion.Neion
import neion.features.RandomStuff
import neion.funnymap.map.ScanUtils
import neion.ui.Configurator
import neion.ui.EditLocationGui
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object Neionssss: BaseCommand("neion", listOf("nn")) {
    private val commands = listOf("edit", "bzsell")
    private val validNames = listOf(
        "spirit_leap",
        "superboom_tnt",
        "ender_pearl",
        "decoy"
    )

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            Config.openGui()
            return
        }
        when (args[0]) {
            "edit" -> Neion.display = EditLocationGui()
            "bzsell" -> RandomStuff.onSell()
            "loadConfig" -> {
                ScanUtils.loadExtras()
                Configurator.loadData()
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos,
    ): MutableList<String> {
        when (args.size) {
            1 -> return getListOfStringsMatchingLastWord(args, commands)
            2 -> return getListOfStringsMatchingLastWord(args, validNames)
        }
        return mutableListOf()
    }
}