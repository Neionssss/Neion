/*
 * Credit Skytils
 * https://github.com/Skytils/SkytilsMod
 */

package neion.mixins;

import neion.commands.ArmorColorCommand;
import neion.features.ArmorColor;
import neion.utils.Utils;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemArmor.class)
abstract class MixinItemArmor {

    @Inject(method = "getColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;"), cancellable = true)
    private void replaceArmorColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ArmorColorCommand.INSTANCE.replaceArmorColor(stack, cir);
    }

    @Inject(method = "getColorFromItemStack", at = @At("HEAD"), cancellable = true)
    private void replaceStackArmorColor(ItemStack stack, int renderPass, CallbackInfoReturnable<Integer> cir) {
        if (renderPass > 0) return;
        ArmorColorCommand.INSTANCE.replaceArmorColor(stack, cir);
    }
}