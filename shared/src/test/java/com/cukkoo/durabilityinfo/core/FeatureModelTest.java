package com.cukkoo.durabilityinfo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureModelTest {
    private static DurabilitySnapshot snapshot(String slot, String item, int damage) {
        return new DurabilitySnapshot(slot, item, item, 100, damage, true, false, false);
    }

    @Test void formatsEveryTooltipStyleAndDisabledFastPath() {
        DurabilityInfoConfig config = ConfigDefaults.vanillaPlus();
        for (TooltipStyle style : TooltipStyle.values()) {
            config.tooltip.style = style;
            assertNotNull(TooltipFormatter.format(snapshot("main", "pick", 53), config));
        }
        config.tooltip.style = TooltipStyle.OFF;
        assertSame(java.util.List.of().getClass(), TooltipFormatter.format(snapshot("main", "pick", 1), config).getClass());
        config.tooltip.style = TooltipStyle.COMPACT;
        TooltipFormatter.TooltipLine compact = TooltipFormatter.format(snapshot("main", "pick", 53), config).getFirst();
        assertEquals("durabilityinfo.tooltip.compact", compact.translationKey());
        assertArrayEquals(new Object[]{47, 100, 47}, compact.arguments());
    }

    @Test void visibilityModesAreDeterministic() {
        DurabilityInfoConfig.HudConfig hud = new DurabilityInfoConfig.HudConfig();
        long now = 10_000;
        for (HudVisibilityMode mode : HudVisibilityMode.values()) {
            hud.visibility = mode;
            assertEquals(HudVisibilityDecider.shouldShow(snapshot("main", "x", 20), hud, now, 9_000),
                    HudVisibilityDecider.shouldShow(snapshot("main", "x", 20), hud, now, 9_000));
        }
        hud.enabled = false;
        assertFalse(HudVisibilityDecider.shouldShow(snapshot("main", "x", 20), hud, now, 9_000));
        hud.enabled = true;
        hud.visibility = HudVisibilityMode.ALWAYS;
        assertTrue(HudVisibilityDecider.shouldShow(snapshot("main", "x", 0), hud, now, 0));
        assertFalse(HudVisibilityDecider.shouldShow(DurabilitySnapshot.empty("empty"), hud, now, 0));
        assertFalse(HudVisibilityDecider.shouldShow(
                new DurabilitySnapshot("main", "stone", "Stone", 100, 0, false, false, false), hud, now, 0));
        assertFalse(HudVisibilityDecider.shouldShow(
                new DurabilitySnapshot("main", "invalid", "Invalid", 0, 0, true, false, false), hud, now, 0));
    }

    @Test void alertCrossingsAreIndependentAndResetAfterRepair() {
        AlertStateTracker tracker = new AlertStateTracker();
        AlertThresholdSet set = new AlertThresholdSet();
        tracker.update(snapshot("main", "pick", 70), set);
        assertEquals("warning", tracker.update(snapshot("main", "pick", 76), set).orElseThrow().level().key());
        assertTrue(tracker.update(snapshot("off", "shield", 96), set).isEmpty());
        assertTrue(tracker.update(snapshot("main", "pick", 77), set).isEmpty());
        tracker.update(snapshot("main", "pick", 60), set);
        assertEquals("warning", tracker.update(snapshot("main", "pick", 76), set).orElseThrow().level().key());
        tracker.clear();
        assertEquals(0, tracker.trackedSlotCount());
    }

    @Test void changeNotificationsIgnoreMovementBoundAndMerge() {
        DurabilityChangeTracker tracker = new DurabilityChangeTracker();
        tracker.update(snapshot("main", "pick", 10), 1000);
        tracker.update(snapshot("main", "pick", 11), 1100);
        tracker.update(snapshot("main", "pick", 12), 1200);
        assertEquals(1, tracker.visible(1200, 5000).size());
        assertEquals(2, tracker.visible(1200, 5000).getFirst().count());
        tracker.update(snapshot("off", "shield", 1), 1300);
        tracker.update(snapshot("off", "shield", 2), 1400);
        tracker.update(snapshot("head", "helmet", 1), 1500);
        tracker.update(snapshot("head", "helmet", 2), 1600);
        tracker.update(snapshot("feet", "boots", 1), 1700);
        tracker.update(snapshot("feet", "boots", 2), 1800);
        assertEquals(3, tracker.visible(1800, 5000).size());
        tracker.clear();
        tracker.update(snapshot("main", "pick", 20), 2000);
        tracker.update(snapshot("off", "pick", 20), 2100);
        assertTrue(tracker.visible(2100, 5000).isEmpty());
        tracker.update(snapshot("off", "pick", 10), 2200);
        assertEquals(10, tracker.visible(2200, 5000).getFirst().delta());
    }

    @Test void presetsAreCompleteAndCustomizable() {
        DurabilityInfoConfig.HudConfig rawDefaults = new DurabilityInfoConfig.HudConfig();
        assertEquals(HudVisibilityMode.ALWAYS, rawDefaults.visibility);
        assertEquals(0.85, rawDefaults.scale);
        for (DurabilityPreset preset : DurabilityPreset.values()) {
            DurabilityInfoConfig config = ConfigValidator.validate(preset.create());
            assertEquals(preset, config.preset);
        }
        assertFalse(DurabilityPreset.MINIMAL.create().notifications.enabled);
        assertTrue(DurabilityPreset.MINING.create().notifications.enabled);
        assertEquals(TooltipStyle.DETAILED, DurabilityPreset.DETAILED.create().tooltip.style);
    }

    @Test void presetPreviewSummaryFieldsRemainMeaningful() {
        DurabilityInfoConfig minimal = DurabilityPreset.MINIMAL.create();
        assertEquals(TooltipStyle.COMPACT, minimal.tooltip.style);
        assertEquals(HudVisibilityMode.RECENTLY_CHANGED, minimal.hud.visibility);
        assertEquals(1, minimal.alerts.armor.descending().stream().filter(AlertThresholdSet.Level::enabled).count());
        assertTrue(minimal.alerts.armor.criticalEnabled);
        assertEquals(OverlayDisplayMode.OFF, minimal.overlays.hotbar);
        assertFalse(minimal.notifications.enabled);

        DurabilityInfoConfig vanillaPlus = DurabilityPreset.VANILLA_PLUS.create();
        assertEquals(TooltipStyle.VANILLA_PLUS, vanillaPlus.tooltip.style);
        assertTrue(vanillaPlus.hud.enabled);
        assertEquals(HudVisibilityMode.ALWAYS, vanillaPlus.hud.visibility);
        assertEquals(0.85, vanillaPlus.hud.scale);
        assertEquals(10, vanillaPlus.alerts.armor.warning);
        assertEquals(OverlayDisplayMode.OFF, vanillaPlus.overlays.inventory);

        DurabilityInfoConfig mining = DurabilityPreset.MINING.create();
        assertEquals(HudVisibilityMode.SMART, mining.hud.visibility);
        assertEquals(OverlayDisplayMode.MINI_BAR, mining.overlays.hotbar);
        assertTrue(mining.notifications.enabled);

        DurabilityInfoConfig combat = DurabilityPreset.COMBAT.create();
        assertEquals(TooltipStyle.COMPACT, combat.tooltip.style);
        assertEquals(HudVisibilityMode.SMART, combat.hud.visibility);
        assertTrue(combat.alerts.hudFlash);

        DurabilityInfoConfig detailed = DurabilityPreset.DETAILED.create();
        assertEquals(HudVisibilityMode.ALWAYS, detailed.hud.visibility);
        assertEquals(OverlayDisplayMode.PERCENTAGE, detailed.overlays.hotbar);
        assertEquals(OverlayDisplayMode.PERCENTAGE, detailed.overlays.inventory);
        assertTrue(detailed.notifications.enabled);

        for (DurabilityPreset unchanged : java.util.List.of(DurabilityPreset.MINIMAL, DurabilityPreset.MINING,
                DurabilityPreset.COMBAT, DurabilityPreset.DETAILED, DurabilityPreset.CUSTOM)) {
            assertEquals(1.0, unchanged.create().hud.scale, unchanged + " scale must remain unchanged");
        }
        assertEquals(HudVisibilityMode.DAMAGED_ONLY, DurabilityPreset.CUSTOM.create().hud.visibility);
    }

    @Test void newInstallAndResetAllUseUpdatedVanillaPlusDefaults() {
        DurabilityInfoConfig config = ConfigValidator.validate(ConfigDefaults.vanillaPlus());
        assertEquals(TooltipStyle.VANILLA_PLUS, config.tooltip.style);
        assertTrue(config.hud.enabled);
        assertEquals(HudVisibilityMode.ALWAYS, config.hud.visibility);
        assertEquals(0.85, config.hud.scale);
        assertEquals(10, config.alerts.armor.warning);
        assertEquals(10, config.alerts.held.warning);
        assertEquals(OverlayDisplayMode.OFF, config.overlays.hotbar);
        assertEquals(OverlayDisplayMode.OFF, config.overlays.inventory);
        assertFalse(config.notifications.enabled);
        assertFalse(config.alerts.chat);
        assertEquals(4, config.hud.backgroundPadding);
    }

    @Test void simpleWarningBindingSynchronizesTargetsWithoutRemovingAdvancedTiers() {
        DurabilityInfoConfig config = DurabilityPreset.COMBAT.create();
        config.alerts.armor.warning = 37;
        config.alerts.held.warning = 22;
        assertFalse(SimpleSettings.warningLevelsMatch(config));
        boolean lowEnabled = config.alerts.armor.lowEnabled;
        SimpleSettings.setWarningLevel(config, 10);
        assertTrue(SimpleSettings.warningLevelsMatch(config));
        assertEquals(10, SimpleSettings.warningLevel(config));
        assertTrue(config.alerts.armor.warningEnabled);
        assertEquals(lowEnabled, config.alerts.armor.lowEnabled);
        DurabilityInfoConfig validated = ConfigValidator.validate(config);
        assertEquals(10, validated.alerts.armor.warning);
        assertTrue(validated.alerts.armor.warning > validated.alerts.armor.low);
        assertTrue(validated.alerts.armor.low > validated.alerts.armor.critical);
        assertTrue(validated.alerts.armor.critical > validated.alerts.armor.lastChance);
    }

    @Test void hudGeometryMatchesRendererForAllLayouts() {
        assertEquals(new HudGeometry.Size(42, 147), HudGeometry.unscaled(HudLayout.VERTICAL, 6, 3));
        assertEquals(new HudGeometry.Size(267, 22), HudGeometry.unscaled(HudLayout.HORIZONTAL, 6, 3));
        assertEquals(new HudGeometry.Size(87, 72), HudGeometry.unscaled(HudLayout.COMPACT_GRID, 6, 3));
        assertEquals(new HudGeometry.Size(534, 44), HudGeometry.scaled(HudLayout.HORIZONTAL, 6, 3, 2.0));
    }

    @Test void layoutClampKeepsHudReachable() {
        assertEquals(new HudPosition(2, 2), HudPosition.clamp(-500, -500, 40, 80, 320, 240, 2));
        assertEquals(new HudPosition(278, 158), HudPosition.clamp(999, 999, 40, 80, 320, 240, 2));
    }

    @Test void overlayFastPathAndThresholdFiltering() {
        var config = new DurabilityInfoConfig.OverlayConfig();
        assertFalse(OverlayDecision.shouldRender(snapshot("main", "x", 50), OverlayDisplayMode.OFF, config));
        config.belowThresholdOnly = true;
        config.threshold = 25;
        assertFalse(OverlayDecision.shouldRender(snapshot("main", "x", 50), OverlayDisplayMode.PERCENTAGE, config));
        assertTrue(OverlayDecision.shouldRender(snapshot("main", "x", 80), OverlayDisplayMode.PERCENTAGE, config));
    }
}
