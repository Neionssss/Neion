package neion.mixins;

import neion.Config;
import neion.utils.Location;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {

    @Shadow private float thirdPersonDistance;

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void hurtCameraEffect(CallbackInfo ci) {
        if (Config.INSTANCE.getHurtCam()) ci.cancel();
    }


    @Inject(method = "orientCamera", at = @At("HEAD"))
    public void orientCamera(CallbackInfo ci) {
        if (Config.INSTANCE.getF5Camera()) thirdPersonDistance = Config.INSTANCE.getF5CameraDistance();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
    public double cameraClip(Vec3 instance, Vec3 vec) {
        return !Config.INSTANCE.getCameraClip() ? instance.distanceTo(vec) : (double) Config.INSTANCE.getF5CameraDistance();
    }
    @Inject(method = "renderHand", at = @At(value = "HEAD"), cancellable = true)
    public void hand(float partialTicks, int xOffset, CallbackInfo ci) {
        if (Config.INSTANCE.getOnlySkyblock() && !Location.INSTANCE.getInSkyblock()) return;
        if (Config.INSTANCE.getRenderHand()) ci.cancel();
    }
}