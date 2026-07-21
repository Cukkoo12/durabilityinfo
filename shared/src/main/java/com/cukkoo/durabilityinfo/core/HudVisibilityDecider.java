package com.cukkoo.durabilityinfo.core;

public final class HudVisibilityDecider {
    private HudVisibilityDecider() {}

    public static boolean shouldShow(DurabilitySnapshot snapshot, DurabilityInfoConfig.HudConfig hud,
                                     long nowMillis, long changedAtMillis) {
        if (!hud.enabled || !DurabilityCalculator.isUsable(snapshot)) return false;
        int percentage = DurabilityCalculator.percentage(snapshot);
        boolean damaged = DurabilityCalculator.damageTaken(snapshot) > 0;
        boolean recent = changedAtMillis > 0
                && nowMillis - changedAtMillis <= (long) (hud.recentlyChangedSeconds * 1000.0);
        return switch (hud.visibility) {
            case ALWAYS -> true;
            case DAMAGED_ONLY -> damaged;
            case BELOW_THRESHOLD -> percentage <= hud.threshold;
            case RECENTLY_CHANGED -> recent;
            case SMART -> (damaged && recent) || percentage <= hud.threshold;
        };
    }
}
