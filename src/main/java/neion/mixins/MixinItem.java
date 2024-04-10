// https://github.com/inglettronald/DulkirMod
package neion.mixins;

import neion.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
abstract class MixinItem {
    @Inject(method = "shouldCauseReequipAnimation", at = @At("HEAD"), cancellable = true, remap = false)
    public void w(ItemStack oldStack, ItemStack newStack, boolean slotChanged, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getCancelReequip()) {
            if (slotChanged && Config.INSTANCE.getShowReEquipAnimationWhenChangingSlots()) return;
            cir.setReturnValue(false);
        }
    }
}
