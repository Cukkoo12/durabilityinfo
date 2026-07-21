package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.client.SlotOverlayRenderer;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HotbarOverlayMixin {
    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void durabilityinfo$hotbarOverlay(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker tracker,
                                              Player player, ItemStack stack, int seed, CallbackInfo ci) {
        var config = DurabilityInfoConfigManager.current();
        SlotOverlayRenderer.render(graphics, stack, x, y, config.overlays.hotbar);
    }
}
