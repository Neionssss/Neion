package neion.mixins;

import neion.events.RenderLivingEntityEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
abstract class MixinRendererLivingEntity {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void renderModel(EntityLivingBase entity, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor, CallbackInfo ci) {
        if (new RenderLivingEntityEvent(entity, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor, mainModel).postAndCatch()) ci.cancel();
    }
}