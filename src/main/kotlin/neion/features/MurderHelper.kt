package neion.features

import neion.events.CheckRenderEntityEvent
import neion.events.RenderLivingEntityEvent
import neion.ui.clickgui.Module
import neion.utils.RenderUtil
import neion.utils.TextUtils
import neion.utils.Utils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object MurderHelper: Module("Murder Helper") {

    private val swords = arrayOf(
        Items.iron_sword,
        Items.stone_sword,
        Items.diamond_sword,
        Items.golden_sword,
        Items.wooden_sword,
        Items.stone_shovel,
        Items.iron_shovel,
        Items.diamond_shovel,
        Items.diamond_axe,
        Items.stick,
        Items.golden_pickaxe,
        Items.golden_carrot,
        Items.carrot_on_a_stick,
        Items.diamond_hoe,
        Items.prismarine_shard,
        Items.carrot,
        Items.name_tag,
        Items.melon,
        Items.fish,
        Items.shears,
        Items.book,
        Items.cookie,
        Items.quartz,
        Items.apple,
        Items.cooked_beef,
        Items.egg,
        Items.blaze_rod,
        Items.boat,
        Items.pumpkin_pie,
        Items.cooked_chicken,
        Items.dye,
        ItemBlock.getItemFromBlock(Blocks.sponge),
        ItemBlock.getItemFromBlock(Blocks.deadbush),
        ItemBlock.getItemFromBlock(Blocks.double_plant),
    )

    val murder = HashSet<EntityOtherPlayerMP>()
    val bowHolder = HashSet<EntityOtherPlayerMP>()
    var wrote = false
    var wrote1 = false

    @SubscribeEvent
    fun mmystery(e: CheckRenderEntityEvent) {
        if (!Utils.inMurderMystery()) return
        val entity = e.entity
        if (entity is EntityItem) RenderUtil.drawEntityBox(entity, Color.yellow, outline = true, fill = false, esp = true)
        if (entity is EntityArmorStand && entity.getEquipmentInSlot(1)?.item == Items.bow) RenderUtil.drawEntityBox(
            entity,
            Color.orange,
            outline = true,
            fill = false,
            esp = true
        )
    }

    // https://i.imgur.com/5t6y5SW.png
    @SubscribeEvent
    fun whileMurderer(e: RenderLivingEntityEvent) {
        if (!Utils.inMurderMystery()) return
        val entity = e.entity as? EntityOtherPlayerMP ?: return
        if (swords.any { mc.thePlayer?.inventory?.hasItem(it)!! }) {
            if (!entity.isPlayerSleeping) RenderUtil.outlineESP(e, Color.green)
            if (entity.getEquipmentInSlot(0)?.item == Items.bow) bowHolder.add(entity)
            if (bowHolder.isNotEmpty()) bowHolder.forEach {
                if (!wrote) {
                    TextUtils.info("${it.name} has bow")
                    wrote = true
                }
                RenderUtil.outlineESP(e, Color.blue)
            }
        } else {
            if (swords.any { entity.getEquipmentInSlot(0)?.item == it }) murder.add(entity)
            if (murder.isNotEmpty()) murder.forEach {
                if (!wrote1) {
                    TextUtils.info("${it.name} is Murderer!")
                    wrote1 = true
                }
                RenderUtil.outlineESP(e, Color.red)
            }
        }
    }
}
