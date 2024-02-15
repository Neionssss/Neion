/*
 *  Credit Hytils Reborn by Polyfrost
 * https://github.com/Polyfrost/Hytils-Reborn
 */

package neion.mixins;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import neion.Config;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;


@Mixin(value = GuiPlayerTabOverlay.class, priority = 990)
abstract class HideTabPingMixin {

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void drawPing(CallbackInfo ci) {
        if (Config.INSTANCE.getCleanerTab()) ci.cancel();
    }

    private String tabFooterAdvertisement = "§aRanks, Boosters & MORE! §r§c§lSTORE.HYPIXEL.NET";
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;listFormattedStringToWidth(Ljava/lang/String;I)Ljava/util/List;"))
    private List<String> hideAdvertisementsInTabHeader(FontRenderer instance, String formatted, int wrapWidth) {
        if (Config.INSTANCE.getCleanerTab() && HypixelUtils.INSTANCE.isHypixel()) {
            if (formatted.contains("§bYou are playing on §r§e§lMC.HYPIXEL.NET")) return Collections.emptyList();
            if (formatted.contains(tabFooterAdvertisement)) return instance.listFormattedStringToWidth(formatted.trim().replace(tabFooterAdvertisement, ""), wrapWidth - 50);
        }
        return instance.listFormattedStringToWidth(formatted, wrapWidth - 50);
    }
}
