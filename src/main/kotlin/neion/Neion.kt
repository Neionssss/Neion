package neion

import neion.commands.*
import neion.features.*
import neion.features.dungeons.*
import neion.features.dungeons.terminals.*
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.funnymap.WitherDoorESP
import neion.funnymap.map.ScanUtils
import neion.ui.Configurator
import neion.ui.GuiRenderer
import neion.utils.APIHandler
import neion.utils.Location
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
            RubixSolver,
            TerminalFeatures,
            ColorsSolver,
            NumbersSolver,
            StartsWith,
            SimonSaysSolver,
            GKey,
            Croesus,
            MostStolenFile,
            ItemESP,
            MurderHelper,
            FreeCam,
            PlayerESP,
            WeirdosSolver,
            TriviaSolver,
            BlazeSolver,
            GuiRenderer,
            DungeonChestProfit,
            Dungeon,
            GFS,
            CustomGUI,
            Location, RunInformation, WitherDoorESP, EditMode, ArmorColor
        ).forEach(MinecraftForge.EVENT_BUS::register)
        listOf(
            FetchCommand,
            ArmorColorCommand,
            FunnyMapCommands,
            EditModeCommand,
            Neionssss
        ).forEach { ClientCommandHandler.instance.registerCommand(it) }
        Configurator.loadData()
        APIHandler.refreshData()
        ScanUtils.loadExtras()
    }


    companion object {
        const val MOD_ID = "neion"
        const val MOD_NAME = "Neion"
        const val MOD_VERSION = "0.0.6"
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
