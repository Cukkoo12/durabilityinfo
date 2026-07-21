package com.cukkoo.durabilityinfo.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ConfigMigration {
    private ConfigMigration() {}

    public static DurabilityInfoConfig migrate(JsonObject source, Gson gson) {
        if (source == null) return ConfigDefaults.vanillaPlus();
        int schema = integer(source, "schemaVersion", 1);
        if (schema >= 2) {
            JsonObject merged = merge(gson.toJsonTree(legacySavedConfigDefaults()).getAsJsonObject(), source);
            return ConfigValidator.validate(gson.fromJson(merged, DurabilityInfoConfig.class));
        }

        DurabilityInfoConfig migrated = legacySavedConfigDefaults();
        boolean numbers = bool(source, "showDurabilityNumbers", true);
        boolean percentage = bool(source, "showPercentage", true);
        boolean bar = bool(source, "showBar", true);
        migrated.tooltip.showNumbers = numbers;
        migrated.tooltip.showPercentage = percentage;
        migrated.tooltip.showBar = bar;
        migrated.tooltip.style = TooltipStyle.CUSTOM;
        migrated.tooltip.showUnbreakable = bool(source, "showOnUnbreakable", false);
        migrated.tooltip.showDamageTaken = bool(source, "showDamageDealt", false);
        migrated.hud.showDamageTaken = bool(source, "showDamageDealt", false);
        migrated.hud.threshold = integer(source, "warningThreshold", 10);
        migrated.alerts.armor.warning = migrated.hud.threshold;
        migrated.alerts.held.warning = migrated.hud.threshold;
        migrated.hud.offsetX = integer(source, "hudOffsetX", 4);
        migrated.hud.offsetY = integer(source, "hudOffsetY", 4);
        migrated.hud.anchor = enumValue(DurabilityInfoConfig.HudAnchor.class,
                string(source, "hudAnchor", "BOTTOM_RIGHT"), DurabilityInfoConfig.HudAnchor.BOTTOM_RIGHT);
        String oldMode = string(source, "hudDisplayMode", "BAR");
        migrated.hud.displayMode = "PERCENTAGE".equalsIgnoreCase(oldMode)
                ? DurabilityInfoConfig.HudDisplayMode.PERCENTAGE
                : DurabilityInfoConfig.HudDisplayMode.MINI_BAR;
        migrated.preset = DurabilityPreset.CUSTOM;
        return ConfigValidator.validate(migrated);
    }

    /** Existing files keep the pre-adjustment HUD fallback values when fields are absent. */
    private static DurabilityInfoConfig legacySavedConfigDefaults() {
        DurabilityInfoConfig defaults = ConfigDefaults.vanillaPlus();
        defaults.hud.visibility = HudVisibilityMode.DAMAGED_ONLY;
        defaults.hud.scale = 1.0;
        return defaults;
    }

    private static JsonObject merge(JsonObject defaults, JsonObject source) {
        for (var entry : source.entrySet()) {
            JsonElement existing = defaults.get(entry.getKey());
            JsonElement incoming = entry.getValue();
            if (existing != null && existing.isJsonObject() && incoming.isJsonObject()) {
                merge(existing.getAsJsonObject(), incoming.getAsJsonObject());
            } else {
                defaults.add(entry.getKey(), incoming.deepCopy());
            }
        }
        return defaults;
    }

    private static boolean bool(JsonObject o, String key, boolean fallback) {
        try { return o.has(key) ? o.get(key).getAsBoolean() : fallback; } catch (RuntimeException ex) { return fallback; }
    }

    private static int integer(JsonObject o, String key, int fallback) {
        try { return o.has(key) ? o.get(key).getAsInt() : fallback; } catch (RuntimeException ex) { return fallback; }
    }

    private static String string(JsonObject o, String key, String fallback) {
        try { return o.has(key) ? o.get(key).getAsString() : fallback; } catch (RuntimeException ex) { return fallback; }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        try { return Enum.valueOf(type, value.toUpperCase()); } catch (RuntimeException ex) { return fallback; }
    }
}
