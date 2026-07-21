package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.client.ClientUiState;
import com.cukkoo.durabilityinfo.client.SlotOverlayRenderer;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerOverlayMixin {
    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void durabilityinfo$slotOverlay(GuiGraphicsExtractor graphics, Slot slot, int left, int top, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || slot.container != client.player.getInventory()) return;
        var config = DurabilityInfoConfigManager.current();
        var mode = ClientUiState.containerOverlayMode(client, config);
        SlotOverlayRenderer.render(graphics, slot.getItem(), left + slot.x, top + slot.y, mode);
    }
}
