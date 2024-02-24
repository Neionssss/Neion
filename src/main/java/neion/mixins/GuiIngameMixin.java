package neion.mixins;

import neion.Config;
import neion.utils.Location;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
abstract class GuiIngameMixin {
    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CallbackInfo ci) {
        if (Config.INSTANCE.getOnlySkyblock() && !Location.INSTANCE.getInSkyblock()) return;
        if (Config.INSTANCE.getHideScoreboard()) ci.cancel();
    }
    @Redirect(method = "renderScoreboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I",ordinal = 1))
    private int ccancel(FontRenderer instance, String text, int x, int y, int color) {return 0;}
}
