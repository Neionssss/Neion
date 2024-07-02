package neion.mixins;

import neion.events.ClickEvent;
import neion.events.PreKeyInputEvent;
import neion.events.PreMouseInputEvent;
import neion.features.CancelInteractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
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
        return CancelInteractions.INSTANCE.shouldCancel(instance,blockPos);
    }
    @Inject(method = "runTick", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V")})
    public void keyPresses(CallbackInfo ci) {
        int k = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) new PreKeyInputEvent(k, Keyboard.getEventCharacter()).postAndCatch();
    }

    @Inject(method = "runTick", at = {@At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I")})
    public void mouseKeyPresses(CallbackInfo ci) {
        if (Mouse.getEventButtonState()) new PreMouseInputEvent(Mouse.getEventButton()).postAndCatch();
    }

}