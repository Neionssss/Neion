package neion.mixins;

import neion.Config;
import neion.events.Render3DEvent;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {

    @Shadow private float thirdPersonDistance;

    @Shadow private float thirdPersonDistanceTemp;


    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void hurtCameraEffect(CallbackInfo ci) {
        if (Config.INSTANCE.getHurtCamIntensity()) ci.cancel();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        new Render3DEvent().postAndCatch();
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    public void orientCamera(CallbackInfo ci) {
            thirdPersonDistance = this.thirdPersonDistanceTemp;
            this.thirdPersonDistanceTemp = Config.INSTANCE.getF5CameraDistance();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
    public double cameraClip(Vec3 instance, Vec3 vec) {
        return !Config.INSTANCE.getCameraClip() ? instance.distanceTo(vec) : (double)Config.INSTANCE.getF5CameraDistance();
    }
}