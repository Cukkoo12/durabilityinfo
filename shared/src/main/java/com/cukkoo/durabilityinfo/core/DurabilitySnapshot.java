package com.cukkoo.durabilityinfo.core;

public record DurabilitySnapshot(
        String slotKey,
        String itemKey,
        String displayName,
        int maxDurability,
        int damage,
        boolean damageable,
        boolean unbreakable,
        boolean empty
) {
    public static DurabilitySnapshot empty(String slotKey) {
        return new DurabilitySnapshot(slotKey, "", "", 0, 0, false, false, true);
    }
}
