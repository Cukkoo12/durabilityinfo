package com.cukkoo.durabilityinfo.core;

public enum DurabilityPreset {
    MINIMAL, VANILLA_PLUS, MINING, COMBAT, DETAILED, CUSTOM;

    public DurabilityInfoConfig create() {
        DurabilityInfoConfig config = base();
        config.preset = this;
        switch (this) {
            case MINIMAL -> {
                config.tooltip.style = TooltipStyle.COMPACT;
                config.hud.visibility = HudVisibilityMode.RECENTLY_CHANGED;
                config.hud.scale = 1.0;
                config.overlays.hotbar = OverlayDisplayMode.OFF;
                config.overlays.inventory = OverlayDisplayMode.OFF;
                config.notifications.enabled = false;
                criticalOnly(config.alerts.armor);
                criticalOnly(config.alerts.held);
            }
            case VANILLA_PLUS -> {
                config.tooltip.style = TooltipStyle.VANILLA_PLUS;
                config.hud.enabled = true;
                config.hud.visibility = HudVisibilityMode.ALWAYS;
                config.hud.scale = 0.85;
                simpleWarning(config.alerts.armor);
                simpleWarning(config.alerts.held);
            }
            case CUSTOM -> {
                config.tooltip.style = TooltipStyle.VANILLA_PLUS;
                config.hud.visibility = HudVisibilityMode.DAMAGED_ONLY;
                config.hud.scale = 1.0;
                simpleWarning(config.alerts.armor);
                simpleWarning(config.alerts.held);
            }
            case MINING -> {
                config.tooltip.style = TooltipStyle.VANILLA_PLUS;
                config.hud.visibility = HudVisibilityMode.SMART;
                config.hud.scale = 1.0;
                config.hud.helmet = config.hud.chestplate = config.hud.leggings = config.hud.boots = false;
                config.hud.mainHand = true;
                config.hud.offhand = false;
                config.overlays.hotbar = OverlayDisplayMode.MINI_BAR;
                config.notifications.enabled = true;
                config.notifications.entireHotbar = true;
                config.alerts.held.warning = 25;
            }
            case COMBAT -> {
                config.tooltip.style = TooltipStyle.COMPACT;
                config.hud.layout = HudLayout.COMPACT_GRID;
                config.hud.scale = 1.0;
                config.hud.mainHand = false;
                config.hud.offhand = true;
                config.hud.visibility = HudVisibilityMode.SMART;
                config.alerts.actionBar = true;
                config.alerts.hudFlash = true;
                config.alerts.armorEnabled = true;
            }
            case DETAILED -> {
                config.tooltip.style = TooltipStyle.DETAILED;
                config.hud.visibility = HudVisibilityMode.ALWAYS;
                config.hud.scale = 1.0;
                config.hud.displayMode = DurabilityInfoConfig.HudDisplayMode.COMBINED;
                config.overlays.hotbar = OverlayDisplayMode.PERCENTAGE;
                config.overlays.inventory = OverlayDisplayMode.PERCENTAGE;
                config.notifications.enabled = true;
            }
        }
        return config;
    }

    private static DurabilityInfoConfig base() {
        return new DurabilityInfoConfig();
    }

    private static void criticalOnly(AlertThresholdSet set) {
        set.warningEnabled = false;
        set.lowEnabled = false;
        set.criticalEnabled = true;
        set.lastChanceEnabled = false;
    }

    private static void simpleWarning(AlertThresholdSet set) {
        set.warning = 10;
        set.low = 5;
        set.critical = 2;
        set.lastChance = 1;
        set.lowEnabled = false;
        set.criticalEnabled = false;
        set.lastChanceEnabled = false;
    }
}
