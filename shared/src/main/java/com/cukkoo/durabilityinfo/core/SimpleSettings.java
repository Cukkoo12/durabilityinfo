package com.cukkoo.durabilityinfo.core;

/** Bindings used by the deliberately small main settings page. */
public final class SimpleSettings {
    private SimpleSettings() {}

    public static boolean warningLevelsMatch(DurabilityInfoConfig config) {
        return config.alerts.armor.warningEnabled
                && config.alerts.held.warningEnabled
                && config.alerts.armor.warning == config.alerts.held.warning;
    }

    public static int warningLevel(DurabilityInfoConfig config) {
        return config.alerts.armor.warning;
    }

    public static void setWarningLevel(DurabilityInfoConfig config, int percentage) {
        int level = Math.max(5, Math.min(100, percentage));
        setWarningLevel(config.alerts.armor, level);
        setWarningLevel(config.alerts.held, level);
    }

    public static void resetVisibleOptions(DurabilityInfoConfig config) {
        DurabilityInfoConfig defaults = ConfigDefaults.vanillaPlus();
        config.tooltip.style = defaults.tooltip.style;
        config.hud.enabled = defaults.hud.enabled;
        setWarningLevel(config, defaults.alerts.armor.warning);
        config.preset = DurabilityPreset.CUSTOM;
    }

    private static void setWarningLevel(AlertThresholdSet set, int level) {
        set.warningEnabled = true;
        set.warning = level;
        set.low = Math.min(set.low, level - 1);
        set.critical = Math.min(set.critical, set.low - 1);
        set.lastChance = Math.min(set.lastChance, set.critical - 1);
        set.lastChance = Math.max(0, set.lastChance);
        set.critical = Math.max(set.lastChance + 1, set.critical);
        set.low = Math.max(set.critical + 1, set.low);
    }
}
