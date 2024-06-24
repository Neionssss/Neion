package neion.mixins;

import neion.features.NoPushOut;
import neion.features.RandomStuff;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerSP.class)
abstract class MixinEntityPlayerSP {

    @Redirect(method = "pushOutOfBlocks", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;noClip:Z"))
    public boolean shouldPrevent(EntityPlayerSP instance) {
        return NoPushOut.INSTANCE.getEnabled() || instance.noClip;
    }
}