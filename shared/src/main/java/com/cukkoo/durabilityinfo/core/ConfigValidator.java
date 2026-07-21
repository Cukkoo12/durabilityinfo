package com.cukkoo.durabilityinfo.core;

public final class ConfigValidator {
    private ConfigValidator() {}

    public static DurabilityInfoConfig validate(DurabilityInfoConfig config) {
        if (config == null) config = ConfigDefaults.vanillaPlus();
        DurabilityInfoConfig defaults = new DurabilityInfoConfig();
        config.schemaVersion = DurabilityInfoConfig.CURRENT_SCHEMA;
        if (config.preset == null) config.preset = DurabilityPreset.CUSTOM;
        if (config.tooltip == null) config.tooltip = defaults.tooltip;
        if (config.hud == null) config.hud = defaults.hud;
        if (config.alerts == null) config.alerts = defaults.alerts;
        if (config.notifications == null) config.notifications = defaults.notifications;
        if (config.overlays == null) config.overlays = defaults.overlays;
        if (config.colors == null) config.colors = defaults.colors;

        if (config.tooltip.style == null) config.tooltip.style = defaults.tooltip.style;
        config.tooltip.barWidth = clamp(config.tooltip.barWidth, 5, 60);

        if (config.hud.displayMode == null) config.hud.displayMode = defaults.hud.displayMode;
        if (config.hud.layout == null) config.hud.layout = defaults.hud.layout;
        if (config.hud.visibility == null) config.hud.visibility = defaults.hud.visibility;
        if (config.hud.anchor == null) config.hud.anchor = defaults.hud.anchor;
        if (config.hud.alignment == null) config.hud.alignment = defaults.hud.alignment;
        if (config.hud.armorOrder == null) config.hud.armorOrder = defaults.hud.armorOrder;
        if (config.hud.handOrder == null) config.hud.handOrder = defaults.hud.handOrder;
        config.hud.offsetX = clamp(config.hud.offsetX, 0, 10000);
        config.hud.offsetY = clamp(config.hud.offsetY, 0, 10000);
        config.hud.scale = finiteClamp(config.hud.scale, 0.5, 2.0, 0.85);
        config.hud.spacing = clamp(config.hud.spacing, 0, 20);
        config.hud.backgroundOpacity = finiteClamp(config.hud.backgroundOpacity, 0.0, 1.0, 0.4);
        config.hud.backgroundPadding = clamp(config.hud.backgroundPadding, 0, 12);
        config.hud.threshold = clamp(config.hud.threshold, 0, 100);
        config.hud.recentlyChangedSeconds = finiteClamp(config.hud.recentlyChangedSeconds, 0.5, 60.0, 4.0);

        if (config.alerts.armor == null) config.alerts.armor = defaults.alerts.armor;
        if (config.alerts.held == null) config.alerts.held = defaults.alerts.held;
        validateAlerts(config.alerts.armor);
        validateAlerts(config.alerts.held);
        config.alerts.soundVolume = finiteClamp(config.alerts.soundVolume, 0.0, 1.0, 1.0);
        config.alerts.flashSeconds = finiteClamp(config.alerts.flashSeconds, 0.1, 10.0, 1.5);
        config.alerts.messageSeconds = finiteClamp(config.alerts.messageSeconds, 0.5, 15.0, 3.0);

        if (config.notifications.position == null) config.notifications.position = defaults.notifications.position;
        config.notifications.durationSeconds = finiteClamp(config.notifications.durationSeconds, 0.5, 10.0, 2.5);
        config.notifications.scale = finiteClamp(config.notifications.scale, 0.5, 2.0, 1.0);

        if (config.overlays.hotbar == null) config.overlays.hotbar = defaults.overlays.hotbar;
        if (config.overlays.inventory == null) config.overlays.inventory = defaults.overlays.inventory;
        if (config.overlays.container == null) config.overlays.container = defaults.overlays.container;
        config.overlays.threshold = clamp(config.overlays.threshold, 0, 100);
        config.overlays.scale = finiteClamp(config.overlays.scale, 0.5, 1.5, 0.75);
        config.overlays.borderThickness = clamp(config.overlays.borderThickness, 1, 3);

        config.colors.criticalBelow = clamp(config.colors.criticalBelow, 0, 98);
        config.colors.lowBelow = clamp(config.colors.lowBelow, config.colors.criticalBelow + 1, 99);
        config.colors.wornBelow = clamp(config.colors.wornBelow, config.colors.lowBelow + 1, 100);
        return config;
    }

    private static void validateAlerts(AlertThresholdSet set) {
        if (set == null) return;
        set.lastChance = clamp(set.lastChance, 0, 97);
        set.critical = clamp(set.critical, set.lastChance + 1, 98);
        set.low = clamp(set.low, set.critical + 1, 99);
        set.warning = clamp(set.warning, set.low + 1, 100);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        if (!Double.isFinite(value)) return fallback;
        return Math.max(min, Math.min(max, value));
    }
}
