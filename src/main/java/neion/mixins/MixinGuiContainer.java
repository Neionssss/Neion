package neion.mixins;

import neion.Config;
import neion.events.GuiContainerEvent;
import neion.features.dungeons.TerminalSolvers;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
abstract class MixinGuiContainer {

    @Shadow protected abstract void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

    @Shadow protected abstract void drawGuiContainerForegroundLayer(int mouseX, int mouseY);

    private final GuiContainer gui = (GuiContainer) (Object) this;

    @Inject(method = "drawSlot", at = @At(value = "HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        if (new GuiContainerEvent.DrawSlotEvent(gui.inventorySlots, slot).postAndCatch()) ci.cancel();
    }

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (new GuiContainerEvent.SlotClickEvent(gui.inventorySlots, slot, slotId, clickedButton, clickType).postAndCatch()) ci.cancel();
    }
    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    private void backgroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (new GuiContainerEvent.BackgroundDrawnEvent(gui.inventorySlots, mouseX, mouseY).postAndCatch()) ci.cancel();
    }
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerBackgroundLayer(FII)V"))
    private void w(GuiContainer instance, float partialTicks, int mouseX, int mouseY) {
        if (!Config.INSTANCE.getTerminalHelper() || TerminalSolvers.INSTANCE.getCurrentTerminal() == TerminalSolvers.Terminal.NONE) drawGuiContainerBackgroundLayer(partialTicks,mouseX,mouseY);
    }
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V"))
    private void a(GuiContainer instance, int mouseX, int mouseY) {
        if (!Config.INSTANCE.getTerminalHelper() || TerminalSolvers.INSTANCE.getCurrentTerminal() == TerminalSolvers.Terminal.NONE) drawGuiContainerForegroundLayer(mouseX,mouseY);
    }
}