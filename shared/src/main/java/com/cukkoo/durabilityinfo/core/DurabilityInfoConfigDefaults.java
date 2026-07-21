package com.cukkoo.durabilityinfo.core;

public final class DurabilityInfoConfigDefaults {
    private DurabilityInfoConfigDefaults() {}

    public static DurabilityInfoConfig create() {
        return ConfigDefaults.vanillaPlus();
    }
}
