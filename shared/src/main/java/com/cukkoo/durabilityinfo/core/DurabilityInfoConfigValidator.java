package com.cukkoo.durabilityinfo.core;

public final class DurabilityInfoConfigValidator {
    private DurabilityInfoConfigValidator() {}

    public static DurabilityInfoConfig validate(DurabilityInfoConfig config) {
        return ConfigValidator.validate(config);
    }
}
