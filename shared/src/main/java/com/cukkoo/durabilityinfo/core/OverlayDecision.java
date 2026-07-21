package com.cukkoo.durabilityinfo.core;

public final class OverlayDecision {
    private OverlayDecision() {}

    public static boolean shouldRender(DurabilitySnapshot snapshot, OverlayDisplayMode mode,
                                       DurabilityInfoConfig.OverlayConfig config) {
        if (mode == OverlayDisplayMode.OFF || !DurabilityCalculator.isUsable(snapshot)) return false;
        int percentage = DurabilityCalculator.percentage(snapshot);
        if (config.hideFullyRepaired && percentage >= 100) return false;
        return !config.belowThresholdOnly || percentage <= config.threshold;
    }
}
