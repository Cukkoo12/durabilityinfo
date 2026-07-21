package com.cukkoo.durabilityinfo.core;

public final class ConfigDefaults {
    private ConfigDefaults() {}

    public static DurabilityInfoConfig vanillaPlus() {
        return DurabilityPreset.VANILLA_PLUS.create();
    }
}
