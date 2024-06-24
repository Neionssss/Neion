package neion.mixins;

import neion.features.Camera;
import neion.features.CustomGUI;
import neion.features.NoHurtCam;
import neion.utils.Location;
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
        if (NoHurtCam.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    public void orientCamera(CallbackInfo ci) {
        if (!Camera.INSTANCE.getEnabled()) return;
        thirdPersonDistance = thirdPersonDistanceTemp;
        thirdPersonDistanceTemp = (float) Camera.INSTANCE.getDistance().getValue();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
    public double cameraClip(Vec3 instance, Vec3 vec) {
        return (!Camera.INSTANCE.getEnabled() || !Camera.INSTANCE.getClip().getEnabled()) ? instance.distanceTo(vec) : Camera.INSTANCE.getDistance().getValue();
    }
    @Inject(method = "renderHand", at = @At(value = "HEAD"), cancellable = true)
    public void hand(float partialTicks, int xOffset, CallbackInfo ci) {
        if (!CustomGUI.INSTANCE.getEnabled() || (CustomGUI.INSTANCE.getOnlySkyblock().getEnabled() && !Location.INSTANCE.getInSkyblock())) return;
        if (CustomGUI.INSTANCE.getHideHand().getEnabled()) ci.cancel();
    }
}