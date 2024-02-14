package neion.mixins;

import neion.Config;
import neion.events.CheckRenderEntityEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Render.class)
abstract class MixinRender {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (new CheckRenderEntityEvent(entityIn, camera, camX, camY, camZ).postAndCatch()) cir.setReturnValue(false);
    }

    @Inject(method = "renderEntityOnFire", at = @At("HEAD"), cancellable = true)
    private void shouldNot(Entity entity, double x, double y, double z, float partialTicks, CallbackInfo ci) {
        if (Config.INSTANCE.getRemoveF3Fire() && entity instanceof EntityPlayerSP) ci.cancel();
    }
}
