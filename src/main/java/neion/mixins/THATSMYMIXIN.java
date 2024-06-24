package neion.mixins;

import neion.features.HideInventoryEffects;
import neion.features.RandomStuff;
import neion.utils.Location;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryEffectRenderer.class)
abstract class THATSMYMIXIN {
    @Inject(method = "updateActivePotionEffects", at = @At(value = "HEAD"), cancellable = true)
    public void haser(CallbackInfo ci) {
        if (HideInventoryEffects.INSTANCE.getOnlyOnSkyblock().getEnabled() && Location.INSTANCE.getInSkyblock()) return;
        if (HideInventoryEffects.INSTANCE.getEnabled()) ci.cancel();
    }
}