package neion.features

import cc.polyfrost.oneconfig.utils.Notifications
import neion.Config
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.events.RenderLivingEntityEvent
import neion.utils.TextUtils
import neion.utils.Utils
import neion.utils.RenderUtil
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object MurderHelper {

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

    val murder = HashSet<Entity>()
    val bowHolder = HashSet<Entity>()
    var wrote = false



    @SubscribeEvent
    fun mmystery(e: CheckRenderEntityEvent<*>) {
        if (!Config.murderHelper || !Utils.inMurderMystery()) return
        val entity = e.entity
        (entity as? EntityItem)?.let { RenderUtil.drawEntityBox(it, Color.yellow, true, false, true) }
        if (entity is EntityArmorStand && entity.getEquipmentInSlot(4)?.item == Items.bow) RenderUtil.drawEntityBox(
            entity,
            Color.orange,
            true,
            false,
            true
        )
    }

    // https://i.imgur.com/5t6y5SW.png
    @SubscribeEvent
    fun whileMurderer(e: RenderLivingEntityEvent) {
        if (!Config.murderHelper || !Utils.inMurderMystery()) return
        val entity = e.entity as? EntityOtherPlayerMP ?: return
        if (swords.any { mc.thePlayer?.inventory?.hasItem(it)!! }) {
            if (!entity.isPlayerSleeping) RenderUtil.outlineESP(e, Color.green)
            if (entity.getEquipmentInSlot(0)?.item == Items.bow) bowHolder.add(entity)
            if (bowHolder.isNotEmpty()) {
                bowHolder.forEach {
                    if (!wrote) {
                        Notifications.INSTANCE.send("Neion", "${it.name} has bow")
                        wrote = true
                    }
                    if (it != entity) return@forEach
                    RenderUtil.outlineESP(e, Color.blue)
                }
            }
        } else {
            if (swords.any { entity.getEquipmentInSlot(0)?.item == it }) murder.add(entity)
            if (murder.isNotEmpty()) {
                murder.forEach {
                    if (!wrote) {
                        TextUtils.info("${it.name} is Murderer!")
                        wrote = true
                    }
                    if (it != entity) return@forEach
                    RenderUtil.outlineESP(e, Color.red)
                }
            }
        }
    }
}
