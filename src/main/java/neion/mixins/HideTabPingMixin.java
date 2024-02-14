/*
 *  Credit Hytils Reborn by Polyfrost
 * https://github.com/Polyfrost/Hytils-Reborn
 */

package neion.mixins;

import neion.Config;
import neion.features.RandomStuff;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiPlayerTabOverlay.class, priority = 990)
abstract class HideTabPingMixin {

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void drawPing(CallbackInfo ci) {
        if (Config.INSTANCE.getCleanerTab()) ci.cancel();
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;listFormattedStringToWidth(Ljava/lang/String;I)Ljava/util/List;"))
    private List<String> hideAds(FontRenderer instance, String formatted, int wrapWidth) {
        return RandomStuff.INSTANCE.modifyHeader(instance,formatted,wrapWidth);
    }
}
