package com.cukkoo.durabilityinfo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(DurabilityInfo.MOD_ID)
public final class DurabilityInfo {
    public static final String MOD_ID = "durabilityinfo";
    public DurabilityInfo(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) DurabilityInfoClient.initialize(container);
    }
}
