/*
 * Credit Skytils
 * https://github.com/Skytils/SkytilsMod
 */
package neion.commands

import neion.Neion.Companion.mc
import neion.features.ArmorColor
import neion.utils.Location.inSkyblock
import neion.utils.TextUtils
import neion.utils.Utils.extraAttributes
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.SyntaxErrorException
import net.minecraft.command.WrongUsageException
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color

object ArmorColorCommand : BaseCommand("color", listOf("armourcolour", "armorcolour", "armourcolor", "armorcolor")) {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/armorcolor <clearall/clear/Int>"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            TextUtils.info("§b" + getCommandUsage(player))
            return
        }
        val item = mc.thePlayer.heldItem ?: throw WrongUsageException("Please hold leather armor piece!")
        val extraAttributes = item.extraAttributes
        if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw WrongUsageException("This item does not have a UUID!")
        val uuid = extraAttributes.getString("uuid")
        when (args[0]) {
            "clearall" -> {
                ArmorColor.armorColors.clear()
                ArmorColor.saveConfig()
                TextUtils.info("§aCleared all your custom armor colors!")
            }

            "clear" -> {
                if ((item.item as? ItemArmor)?.armorMaterial != ItemArmor.ArmorMaterial.LEATHER) throw WrongUsageException("Please hold leather armor piece!")
                if (ArmorColor.armorColors.contains(uuid)) {
                    ArmorColor.armorColors.remove(uuid)
                    ArmorColor.saveConfig()
                } else TextUtils.info("§cThat item doesn't have a custom color!")
            }

            else -> {
                if (!inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
                if ((item.item as? ItemArmor)?.armorMaterial != ItemArmor.ArmorMaterial.LEATHER) TextUtils.info("You must hold a leather armor piece to use this command")
                val color = try {
                    Color(args[0].toInt())
                } catch (e: IllegalArgumentException) {
                    throw SyntaxErrorException("§cOnly integer works")
                }
                ArmorColor.armorColors[uuid] = color.rgb
                ArmorColor.saveConfig()
                TextUtils.info("§aSet the color of your ${item.displayName}§a to ${args[0]}!")
            }
        }
    }

    fun replaceArmorColor(stack: ItemStack, cir: CallbackInfoReturnable<Int>) {
        if (!inSkyblock) return
        val extraAttributes = stack.extraAttributes ?: return
        val uuid = extraAttributes.getString("uuid")
        if (extraAttributes.hasKey("uuid") && ArmorColor.armorColors.containsKey(uuid)) cir.returnValue = ArmorColor.armorColors[uuid]!!.toInt()
    }
}