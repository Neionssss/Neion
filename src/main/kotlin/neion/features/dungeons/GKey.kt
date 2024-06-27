package neion.features.dungeons

import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.ui.clickgui.settings.NumberSetting
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard

object GKey: Module("GKey", category = Category.DUNGEON, hasBind = true) {

    private val delay = NumberSetting("Delay", 200.0, min = 0.0, max = 1500.0)
    private val range = NumberSetting("Range", 5.0, min = 1.0, max = 15.0)

    init {
        addSettings(delay, range)
    }

    override fun keyBind() {}

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        if (!Keyboard.isKeyDown(keyCode)) return
        val rt = mc.thePlayer?.rayTrace(range.value, 0.0f) ?: return
        if (!blacklist.contains(mc.theWorld.getBlockState(rt.blockPos)?.block) && System.currentTimeMillis() - last0l > delay.value) {
            mc.theWorld.setBlockState(rt.blockPos, Blocks.air.defaultState)
            last0l = System.currentTimeMillis()
        }
    }

    private var last0l = 0L


    // https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/GhostBlock.kt
    private val blacklist = listOf(
        Blocks.acacia_door,
        Blocks.anvil,
        Blocks.beacon,
        Blocks.bed,
        Blocks.birch_door,
        Blocks.brewing_stand,
        Blocks.brown_mushroom,
        Blocks.chest,
        Blocks.command_block,
        Blocks.crafting_table,
        Blocks.dark_oak_door,
        Blocks.daylight_detector,
        Blocks.daylight_detector_inverted,
        Blocks.dispenser,
        Blocks.dropper,
        Blocks.enchanting_table,
        Blocks.ender_chest,
        Blocks.furnace,
        Blocks.hopper,
        Blocks.jungle_door,
        Blocks.lever,
        Blocks.noteblock,
        Blocks.oak_door,
        Blocks.powered_comparator,
        Blocks.powered_repeater,
        Blocks.red_mushroom,
        Blocks.standing_sign,
        Blocks.stone_button,
        Blocks.trapdoor,
        Blocks.trapped_chest,
        Blocks.unpowered_comparator,
        Blocks.unpowered_repeater,
        Blocks.wall_sign,
        Blocks.wooden_button,
        Blocks.air,
        Blocks.skull
    )
}
