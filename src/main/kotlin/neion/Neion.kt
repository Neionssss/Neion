package neion

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import neion.commands.ArmorColorCommand
import neion.commands.EditModeCommand
import neion.commands.MapCommands
import neion.commands.Neionssss
import neion.features.*
import neion.features.dungeons.*
import neion.funnymap.WitherDoorESP
import neion.funnymap.map.MapUtils
import neion.ui.EditLocationGui
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
    name = "Neion",
    version = "0.0.8",
)

class Neion {

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        MapConfig.initialize()
        Config.initialize()
        listOf(
            this,
            RandomStuff,
            Trapper,
            TerminalSolvers,
            SimonSaysSolver,
            Croesus,
            EntityHider,
            MurderHelper,
            FreeCam,
            WeirdosSolver,
            TriviaSolver,
            BlazeSolver,
            GuiRenderer,
            DungeonChestProfit,
            CustomGUI,
            WitherDoorESP, EditMode, ArmorColor, TeleportMazeSolver
        ).forEach(MinecraftForge.EVENT_BUS::register)
        listOf(
            ArmorColorCommand,
            MapCommands,
            EditModeCommand,
            Neionssss
        ).forEach(ClientCommandHandler.instance::registerCommand)
        ArmorColor.loadConfig()
        APIHandler.refreshData()
        MapUtils.loadExtras()
        EditLocationGui.file.createNewFile()
        with(EditLocationGui.file.bufferedReader().readText()) {
            if (this != "") GuiRenderer.positions = gson.fromJson(this, object : TypeToken<HashMap<String, Triple<Int, Int, Float>>>() {}.type)
        }
    }

    companion object {
        const val MOD_ID = "neion"

        @JvmStatic
        val mc: Minecraft by lazy {
            Minecraft.getMinecraft()
        }

        val modDir by lazy { File(File(mc.mcDataDir, "config"), "neion").also { it.mkdirs() } }
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        var display: GuiScreen? = null
    }
}
