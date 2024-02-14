package neion.mixins;

import neion.Config;
import neion.utils.Location;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
abstract class GuiIngameMixin {
    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CallbackInfo ci) {
        if (Config.INSTANCE.getOnlySkyblock() && !Location.INSTANCE.getInSkyblock()) return;
        if (Config.INSTANCE.getHideScoreboard()) ci.cancel();
    }
}
