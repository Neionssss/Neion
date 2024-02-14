package neion.commands

import neion.Config
import neion.Neion
import neion.features.JasperESP
import neion.features.RandomStuff
import neion.funnymap.map.ScanUtils
import neion.ui.Configurator
import neion.ui.EditLocationGui
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object Neionssss: BaseCommand("neion", listOf("nn")) {
    private val commands = listOf("loadConfig", "editGUI", "bzsell", "stopScan", "restartScan")
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
            "editGUI" -> Neion.display = EditLocationGui()
            "bzsell" -> RandomStuff.onSell()
            "loadConfig" -> {
                ScanUtils.loadExtras()
                Configurator.loadData()
            }
            "stopScan" -> JasperESP.stopped = true
            "restartScan" -> {
                JasperESP.stopped = false
                JasperESP.scanning = false
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