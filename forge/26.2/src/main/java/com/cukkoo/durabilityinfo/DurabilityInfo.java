package com.cukkoo.durabilityinfo;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DurabilityInfo.MOD_ID)
public final class DurabilityInfo {
    public static final String MOD_ID = "durabilityinfo";
    public DurabilityInfo() {
        if (FMLEnvironment.dist == Dist.CLIENT) DurabilityInfoClient.initialize();
    }
}
