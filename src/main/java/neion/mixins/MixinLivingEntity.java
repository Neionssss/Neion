package neion.mixins;

import neion.Config;
import neion.features.RandomStuff;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
abstract class MixinLivingEntity {
    @Inject(method = "getArmSwingAnimationEnd", at = @At("HEAD"), cancellable = true)
        private void ohEntity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max((int)(RandomStuff.INSTANCE.length()  * Math.exp(-Config.INSTANCE.getItemSwingSpeed())), 1));
    }
}
