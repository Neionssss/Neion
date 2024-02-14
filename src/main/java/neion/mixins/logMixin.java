package neion.mixins;

import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
abstract class logMixin {
    @Inject(method = "runTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false), cancellable = true)
    private static <V> void interceptTaskExceptions(CallbackInfoReturnable<V> cir) {cir.setReturnValue(null);}
}