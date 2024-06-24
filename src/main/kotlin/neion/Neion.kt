package neion

import neion.commands.ArmorColorCommand
import neion.commands.EditModeCommand
import neion.commands.MapCommands
import neion.commands.Neionssss
import neion.features.ArmorColor
import neion.features.RandomStuff
import neion.features.RandomStuff.autoRunBind
import neion.features.RandomStuff.autoSellBind
import neion.features.RandomStuff.equipmentBind
import neion.features.RandomStuff.peekBind
import neion.features.RandomStuff.petsBind
import neion.features.RandomStuff.tradesBind
import neion.features.RandomStuff.wardrobeBind
import neion.features.dungeons.EditMode
import neion.funnymap.Dungeon
import neion.funnymap.RunInformation
import neion.funnymap.WitherDoorESP
import neion.ui.clickgui.ModuleConfig
import neion.ui.clickgui.ModuleManager
import neion.utils.APIHandler
import neion.utils.APIHandler.getResponse
import neion.utils.APIHandler.quizdata
import neion.utils.ExtrasConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import java.io.File


@Mod(
    modid = Neion.MOD_ID,
    name = "Neion",
    version = "0.1.0",
)

class Neion {

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        listOf(
            this,
            RandomStuff,
            Dungeon,
            RunInformation, WitherDoorESP, EditMode, ArmorColor, ModuleManager
        ).forEach(MinecraftForge.EVENT_BUS::register)
        listOf(
            ArmorColorCommand,
            MapCommands,
            EditModeCommand,
            Neionssss
        ).forEach(ClientCommandHandler.instance::registerCommand)
        listOf(peekBind, tradesBind, petsBind, equipmentBind, wardrobeBind, autoRunBind, autoSellBind).forEach(ClientRegistry::registerKeyBinding)
        ArmorColor.loadConfig()
        APIHandler.refreshData()
        quizdata = getResponse("https://data.skytils.gg/solvers/oruotrivia.json")
        ExtrasConfig.loadExtras()
        ModuleConfig(modDir).loadConfig()
    }

    companion object {
        const val MOD_ID = "neion"
        const val CHAT_PREFIX = "§f§0[Neion]§f§r"

        @JvmStatic
        val mc: Minecraft by lazy { Minecraft.getMinecraft() }

        val modDir by lazy { File(File(mc.mcDataDir, "config"), "neion").also { it.mkdirs() } }
        var display: GuiScreen? = null
    }
}
