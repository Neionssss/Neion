package neion.mixins;

import neion.events.ClickEvent;
import neion.features.dungeons.CancelInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MixinMinecraft {

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void onLeftClick(CallbackInfo ci) {
        if (new ClickEvent.LeftClickEvent().postAndCatch()) ci.cancel();
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER), cancellable = true)
    private void onRightClick(CallbackInfo ci) {
        if (new ClickEvent.RightClickEvent().postAndCatch()) ci.cancel();
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    private void onMiddleClick(CallbackInfo ci) {
        if (new ClickEvent.MiddleClickEvent().postAndCatch()) ci.cancel();
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;isAirBlock(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean shouldCancelInteract(WorldClient instance, BlockPos blockPos) {
        return CancelInteract.INSTANCE.shouldPriotizeAbilityHook(instance, blockPos);
    }
}