package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.core.OverlayDisplayMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackVanillaBarMixin {
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void durabilityinfo$optionalVanillaBarReplacement(CallbackInfoReturnable<Boolean> cir) {
        var overlays = DurabilityInfoConfigManager.current().overlays;
        if (overlays.replaceVanillaBar && (overlays.hotbar != OverlayDisplayMode.OFF
                || overlays.inventory != OverlayDisplayMode.OFF || overlays.container != OverlayDisplayMode.OFF)) {
            cir.setReturnValue(false);
        }
    }
}
