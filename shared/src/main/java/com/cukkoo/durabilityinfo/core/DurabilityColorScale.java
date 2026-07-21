package com.cukkoo.durabilityinfo.core;

public final class DurabilityColorScale {
    public enum Band { HEALTHY, WORN, LOW, CRITICAL }

    private DurabilityColorScale() {}

    public static Band band(int percentage, DurabilityInfoConfig.ColorConfig colors) {
        int value = DurabilityCalculator.clamp(percentage, 0, 100);
        if (value <= colors.criticalBelow) return Band.CRITICAL;
        if (value <= colors.lowBelow) return Band.LOW;
        if (value <= colors.wornBelow) return Band.WORN;
        return Band.HEALTHY;
    }

    public static int argb(int percentage, DurabilityInfoConfig.ColorConfig colors) {
        return switch (band(percentage, colors)) {
            case HEALTHY -> 0xFF55FF55;
            case WORN -> 0xFFFFAA00;
            case LOW -> 0xFFFF5555;
            case CRITICAL -> 0xFFFF2020;
        };
    }
}
