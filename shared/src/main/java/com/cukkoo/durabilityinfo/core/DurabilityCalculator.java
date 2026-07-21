package com.cukkoo.durabilityinfo.core;

public final class DurabilityCalculator {
    private DurabilityCalculator() {}

    public static int remaining(DurabilitySnapshot snapshot) {
        if (!isUsable(snapshot)) return 0;
        return clamp(snapshot.maxDurability() - snapshot.damage(), 0, snapshot.maxDurability());
    }

    public static int damageTaken(DurabilitySnapshot snapshot) {
        if (!isUsable(snapshot)) return 0;
        return clamp(snapshot.damage(), 0, snapshot.maxDurability());
    }

    public static int percentage(DurabilitySnapshot snapshot) {
        if (!isUsable(snapshot)) return 0;
        return (int) Math.min(100L, Math.max(0L,
                ((long) remaining(snapshot) * 100L) / snapshot.maxDurability()));
    }

    public static boolean isUsable(DurabilitySnapshot snapshot) {
        return snapshot != null && !snapshot.empty() && snapshot.damageable() && snapshot.maxDurability() > 0;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
