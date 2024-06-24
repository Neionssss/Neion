package neion.mixins;

import io.netty.channel.ChannelHandlerContext;
import neion.events.ChatEvent;
import neion.events.PacketReceiveEvent;
import neion.events.PacketSentEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkManager.class, priority = 800)
abstract class MixinNetworkManager {

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (new PacketReceiveEvent(packet).postAndCatch()) ci.cancel();
        if (packet instanceof S02PacketChat) new ChatEvent((S02PacketChat) packet).postAndCatch();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (new PacketSentEvent(packet).postAndCatch()) ci.cancel();
    }
}
