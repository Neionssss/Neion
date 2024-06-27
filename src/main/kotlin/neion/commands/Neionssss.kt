package neion.commands

import neion.Neion
import neion.features.ArmorColor
import neion.features.ClickGui
import neion.ui.EditHudGUI
import neion.ui.clickgui.ClickGUI
import neion.ui.clickgui.ModuleConfig
import neion.utils.APIHandler
import neion.utils.ExtrasConfig
import neion.utils.TextUtils
import neion.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object Neionssss: BaseCommand("neion", listOf("nn")) {
    private val commands = listOf("loadConfig", "edit", "refreshPrices")

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            Neion.display = ClickGUI()
            return
        }
        when (args[0]) {
            "loadConfig" -> {
                ExtrasConfig.loadExtras()
                ArmorColor.loadConfig()
            }
            "edit" -> Neion.display = EditHudGUI
            "refreshPrices" -> APIHandler.refreshData()
            "apiKey" -> {
                ClickGui.apiKey.text = args[1]
                ModuleConfig(Neion.modDir).saveConfig()
            }
            "fetch" -> Utils.fetchEVERYWHERE(args[1])?.let { Utils.fn(it) }?.let { TextUtils.info(it) }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos,
    ): MutableList<String> {
        when (args.size) {
            1 -> return getListOfStringsMatchingLastWord(args, commands)
        }
        return mutableListOf()
    }
}