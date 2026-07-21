package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.client.ClientRuntime;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public final class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void durabilityinfo$tick(CallbackInfo ci) {
        ClientRuntime.tick((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void durabilityinfo$close(CallbackInfo ci) {
        ClientRuntime.clear();
    }
}
