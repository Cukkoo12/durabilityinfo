package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.client.HudRenderer;
import com.cukkoo.durabilityinfo.client.ClientRuntime;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class DurabilityInfoHUDMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"))
    private void durabilityinfo$render(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
        var config = DurabilityInfoConfigManager.current();
        boolean hidden = config.hud.hideWhenGameHudHidden && Minecraft.getInstance().gui.hud.isHidden();
        if (!hidden) {
            HudRenderer.render(graphics);
            ClientRuntime.renderNotifications(graphics);
        }
    }
}
