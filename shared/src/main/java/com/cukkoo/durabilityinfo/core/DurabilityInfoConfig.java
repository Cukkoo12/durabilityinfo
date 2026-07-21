package com.cukkoo.durabilityinfo.core;

public final class DurabilityInfoConfig {
    public static final int CURRENT_SCHEMA = 2;

    public int schemaVersion = CURRENT_SCHEMA;
    public DurabilityPreset preset = DurabilityPreset.VANILLA_PLUS;
    public TooltipConfig tooltip = new TooltipConfig();
    public HudConfig hud = new HudConfig();
    public AlertConfig alerts = new AlertConfig();
    public NotificationConfig notifications = new NotificationConfig();
    public OverlayConfig overlays = new OverlayConfig();
    public ColorConfig colors = new ColorConfig();

    public static final class TooltipConfig {
        public TooltipStyle style = TooltipStyle.VANILLA_PLUS;
        public boolean showNumbers = true;
        public boolean showPercentage = true;
        public boolean showBar = false;
        public boolean showUnbreakable = false;
        public boolean showDamageTaken = false;
        public boolean hideFullyRepaired = false;
        public int barWidth = 20;
    }

    public static final class HudConfig {
        public boolean enabled = true;
        public boolean helmet = true;
        public boolean chestplate = true;
        public boolean leggings = true;
        public boolean boots = true;
        public boolean mainHand = true;
        public boolean offhand = true;
        public HudDisplayMode displayMode = HudDisplayMode.MINI_BAR;
        public HudLayout layout = HudLayout.VERTICAL;
        public HudVisibilityMode visibility = HudVisibilityMode.ALWAYS;
        public HudAnchor anchor = HudAnchor.BOTTOM_RIGHT;
        public HudAlignment alignment = HudAlignment.CENTER;
        public ArmorOrder armorOrder = ArmorOrder.HEAD_TO_FEET;
        public HandOrder handOrder = HandOrder.MAIN_THEN_OFFHAND;
        public int offsetX = 4;
        public int offsetY = 4;
        public double scale = 0.85;
        public int spacing = 3;
        public boolean background = true;
        public double backgroundOpacity = 0.4;
        public int backgroundPadding = 4;
        public boolean showIcons = true;
        public boolean textShadow = true;
        public boolean snapToGuides = true;
        public int threshold = 10;
        public double recentlyChangedSeconds = 4.0;
        public boolean hideCreative = false;
        public boolean hideSpectator = true;
        public boolean hideDebug = true;
        public boolean hideInContainers = false;
        public boolean hideWhenGameHudHidden = true;
        public boolean onlyHeld = false;
        public boolean onlyArmor = false;
        public boolean showDamageTaken = false;
    }

    public static final class AlertConfig {
        public boolean armorEnabled = true;
        public boolean heldEnabled = true;
        public boolean mainHandEnabled = true;
        public boolean offhandEnabled = true;
        public AlertThresholdSet armor = new AlertThresholdSet();
        public AlertThresholdSet held = new AlertThresholdSet();
        public boolean sound = true;
        public boolean actionBar = true;
        public boolean chat = false;
        public boolean hudFlash = true;
        public double soundVolume = 1.0;
        public double flashSeconds = 1.5;
        public double messageSeconds = 3.0;
    }

    public static final class NotificationConfig {
        public boolean enabled = false;
        public boolean mainHand = true;
        public boolean offhand = true;
        public boolean armor = true;
        public boolean entireHotbar = false;
        public double durationSeconds = 2.5;
        public NotificationPosition position = NotificationPosition.TOP_RIGHT;
        public double scale = 1.0;
    }

    public static final class OverlayConfig {
        public OverlayDisplayMode hotbar = OverlayDisplayMode.OFF;
        public OverlayDisplayMode inventory = OverlayDisplayMode.OFF;
        public OverlayDisplayMode container = OverlayDisplayMode.OFF;
        public boolean hideFullyRepaired = true;
        public boolean belowThresholdOnly = false;
        public int threshold = 25;
        public boolean textShadow = true;
        public double scale = 0.75;
        public int borderThickness = 1;
        public boolean replaceVanillaBar = false;
    }

    public static final class ColorConfig {
        public int wornBelow = 50;
        public int lowBelow = 25;
        public int criticalBelow = 5;
    }

    public enum HudDisplayMode { MINI_BAR, PERCENTAGE, REMAINING, REMAINING_AND_MAX, COMBINED }
    public enum HudAnchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum HudAlignment { START, CENTER, END }
    public enum ArmorOrder { HEAD_TO_FEET, FEET_TO_HEAD }
    public enum HandOrder { MAIN_THEN_OFFHAND, OFFHAND_THEN_MAIN }
    public enum NotificationPosition { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
}
