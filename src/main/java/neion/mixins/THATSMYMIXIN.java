package neion.mixins;

import neion.Config;
import neion.utils.Location;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryEffectRenderer.class)
abstract class THATSMYMIXIN {
    @Inject(method = "updateActivePotionEffects", at = @At(value = "HEAD"), cancellable = true)
    public void a(CallbackInfo ci) {
        if (Config.INSTANCE.getHidePotionEffects() && Location.INSTANCE.getInSkyblock()) ci.cancel();
    }
}