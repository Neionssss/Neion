package neion

import neion.commands.*
import neion.features.*
import neion.features.dungeons.*
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.funnymap.WitherDoorESP
import neion.funnymap.map.ScanUtils
import neion.ui.Configurator
import neion.ui.GuiRenderer
import neion.utils.APIHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import java.io.File


@Mod(
    modid = Neion.MOD_ID,
    name = Neion.MOD_NAME,
    version = Neion.MOD_VERSION,
)

class Neion {

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        Config.init()
        FMConfig.init()
        listOf(
            this,
            RandomStuff,
            Trapper,
            JasperESP,
            TerminalSolvers,
            SimonSaysSolver,
            GKey,
            Croesus,
            MostStolenFile,
            MurderHelper,
            FreeCam,
            WeirdosSolver,
            TriviaSolver,
            BlazeSolver,
            GuiRenderer,
            DungeonChestProfit,
            Dungeon,
            CustomGUI,
            RunInformation, WitherDoorESP, EditMode, ArmorColor, EtherwarpOverlay, TeleportMazeSolver
        ).forEach(MinecraftForge.EVENT_BUS::register)
        listOf(
            FetchCommand,
            ArmorColorCommand,
            MapCommands,
            EditModeCommand,
            Neionssss
        ).forEach(ClientCommandHandler.instance::registerCommand)
        Configurator.loadData()
        APIHandler.refreshData()
        ScanUtils.loadExtras()
    }

    companion object {
        const val MOD_ID = "neion"
        const val MOD_NAME = "Neion"
        const val MOD_VERSION = "0.0.7"
        const val CHAT_PREFIX = "§f§0[Neion]§f§r"

        @JvmStatic
        val mc: Minecraft by lazy {
            Minecraft.getMinecraft()
        }

        val modDir by lazy {
            File(File(mc.mcDataDir, "config"), "neion").also { it.mkdirs() }
        }
        var display: GuiScreen? = null
    }
}
