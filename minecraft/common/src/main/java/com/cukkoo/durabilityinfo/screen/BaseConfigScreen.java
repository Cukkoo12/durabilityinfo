package com.cukkoo.durabilityinfo.screen;

import com.cukkoo.durabilityinfo.core.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public abstract class BaseConfigScreen extends Screen {
    private enum AdvancedSection { TOOLTIP, HUD, WARNINGS, OVERLAYS, NOTIFICATIONS, PRESETS }
    private record TextLine(int x, int y, Component text, int color) {}
    private record Surface(int x, int y, int width, int height, boolean accent, boolean strong) {}

    private static final int ACCENT = 0xFF59AFFF;
    private static final int TEXT = 0xFFF2F2F2;
    private static final int MUTED = 0xFFADAFB8;

    private final Screen parent;
    protected DurabilityInfoConfig draft;
    private boolean advanced;
    private AdvancedSection section = AdvancedSection.HUD;
    private int scroll;
    private int rows;
    private int maxScroll;
    private int rowHeight;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelBottom;
    private int contentTop;
    private int contentBottom;
    private int contentLeft;
    private int contentRight;
    private int footerTop;
    private int sectionTitleY;
    private final List<TextLine> textLines = new ArrayList<>();
    private final List<Surface> surfaces = new ArrayList<>();
    private final List<Surface> navigationHighlights = new ArrayList<>();
    private DurabilityPreset pendingPreset;
    private boolean alertChannelsExpanded;

    protected BaseConfigScreen(Screen parent) {
        super(Component.translatable("durabilityinfo.config.title"));
        this.parent = parent;
        this.draft = DurabilityInfoConfigManager.copyCurrent();
    }

    @Override protected void init() {
        textLines.clear();
        surfaces.clear();
        navigationHighlights.clear();
        rows = 0;
        calculateFrame();

        if (advanced) {
            addAdvancedNavigation();
            switch (section) {
                case TOOLTIP -> tooltipRows();
                case HUD -> hudRows();
                case WARNINGS -> warningRows();
                case OVERLAYS -> overlayRows();
                case NOTIFICATIONS -> notificationRows();
                case PRESETS -> presetRows();
            }
        } else {
            mainRows();
        }

        int visibleRows = Math.max(1, (contentBottom - contentTop) / rowHeight);
        maxScroll = Math.max(0, rows - visibleRows);
        if (scroll > maxScroll) {
            scroll = maxScroll;
            rebuildWidgets();
            return;
        }
        if (maxScroll > 0) addScrollButtons();
        addFooter();
    }

    private void calculateFrame() {
        panelWidth = Math.max(1, Math.min(620, width - 16));
        panelLeft = (width - panelWidth) / 2;
        panelTop = 8;
        panelBottom = Math.max(panelTop + 1, height - 8);
        contentLeft = panelLeft + 10;
        contentRight = panelLeft + panelWidth - 10;
        boolean compactFooter = panelWidth < 460;
        int footerHeight = compactFooter ? 58 : 34;
        footerTop = panelBottom - footerHeight;

        if (advanced) {
            boolean oneRowNavigation = panelWidth >= 540;
            int navigationRows = oneRowNavigation ? 1 : 2;
            int navigationTop = panelTop + 31;
            sectionTitleY = navigationTop + navigationRows * 22 + 4;
            contentTop = sectionTitleY + 28;
        } else {
            sectionTitleY = panelTop + 8;
            contentTop = panelTop + 43;
        }
        contentBottom = Math.max(contentTop, footerTop - 5);
        rowHeight = advanced
                ? (panelWidth < 430 ? 44 : 24)
                : Math.max(23, Math.min(44, (contentBottom - contentTop) / 5));
    }

    private void mainRows() {
        mainEnumRow("Tooltip Style", "Choose how durability appears in item tooltips.",
                draft.tooltip.style, TooltipStyle.values(), value -> draft.tooltip.style = value);
        mainToggleRow("Show HUD", "Show equipped-item durability while playing.",
                draft.hud.enabled, value -> draft.hud.enabled = value);
        int warning = SimpleSettings.warningLevel(draft);
        mainSliderRow("Warning Level", "Warn when armor or held items reach this durability.", 5, 100, warning,
                value -> SimpleSettings.warningLevelsMatch(draft) && SimpleSettings.warningLevel(draft) >= 5
                        ? (int) Math.round(value) + "%"
                        : Component.translatable("durabilityinfo.config.value.mixed").getString(),
                value -> SimpleSettings.setWarningLevel(draft, (int) Math.round(value)));
        mainActionRow("Edit HUD Position", "Drag and resize a preview without changing expert options.",
                "Open Editor", () -> showScreen(new HudLayoutEditorScreen(this, draft)));
        mainActionRow("Advanced Settings", "Overlays, notifications, visibility, warnings, and appearance.",
                "Open", () -> {
                    advanced = true;
                    section = AdvancedSection.HUD;
                    scroll = 0;
                    rebuildWidgets();
                });
    }

    private void addAdvancedNavigation() {
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
            advanced = false;
            scroll = 0;
            pendingPreset = null;
            rebuildWidgets();
        }).pos(panelLeft + 10, panelTop + 6).width(88).build());

        boolean oneRow = panelWidth >= 540;
        int columns = oneRow ? AdvancedSection.values().length : 3;
        int navigationTop = panelTop + 31;
        int gap = 3;
        int navigationWidth = panelWidth - 20;
        int buttonWidth = (navigationWidth - (columns - 1) * gap) / columns;
        for (AdvancedSection candidate : AdvancedSection.values()) {
            int column = candidate.ordinal() % columns;
            int row = candidate.ordinal() / columns;
            int x = panelLeft + 10 + column * (buttonWidth + gap);
            int y = navigationTop + row * 22;
            Button button = Button.builder(sectionComponent(candidate), pressed -> {
                if (section != candidate) {
                    section = candidate;
                    scroll = 0;
                    pendingPreset = null;
                    rebuildWidgets();
                }
            }).pos(x, y).width(buttonWidth).build();
            button.setTooltip(Tooltip.create(sectionHelp(candidate)));
            addRenderableWidget(button);
            if (candidate == section) navigationHighlights.add(new Surface(x - 1, y - 1, buttonWidth + 2, 22, true, true));
        }
    }

    private void tooltipRows() {
        sectionRow("Tooltip Content");
        enumRow("Tooltip Style", draft.tooltip.style, TooltipStyle.values(), value -> draft.tooltip.style = value);
        toggleRow("Show Durability Numbers", draft.tooltip.showNumbers, value -> draft.tooltip.showNumbers = value);
        toggleRow("Show Percentage", draft.tooltip.showPercentage, value -> draft.tooltip.showPercentage = value);
        toggleRow("Text Durability Bar", draft.tooltip.showBar, value -> draft.tooltip.showBar = value);
        toggleRow("Label Unbreakable Items", draft.tooltip.showUnbreakable, value -> draft.tooltip.showUnbreakable = value);
        toggleRow("Show Damage Used", draft.tooltip.showDamageTaken, value -> draft.tooltip.showDamageTaken = value);
        toggleRow("Hide at Full Durability", draft.tooltip.hideFullyRepaired, value -> draft.tooltip.hideFullyRepaired = value);
        intRow("Text Bar Width", 5, 60, draft.tooltip.barWidth, "", value -> draft.tooltip.barWidth = value);
        sectionRow("Durability Colors");
        intRow("Worn Color Below", 2, 100, draft.colors.wornBelow, "%", value -> draft.colors.wornBelow = value);
        intRow("Low Color Below", 1, 99, draft.colors.lowBelow, "%", value -> draft.colors.lowBelow = value);
        intRow("Critical Color Below", 0, 98, draft.colors.criticalBelow, "%", value -> draft.colors.criticalBelow = value);
    }

    private void hudRows() {
        sectionRow("Items Shown");
        toggleRow("Show HUD", draft.hud.enabled, value -> draft.hud.enabled = value);
        toggleRow("Helmet", draft.hud.helmet, value -> draft.hud.helmet = value);
        toggleRow("Chestplate", draft.hud.chestplate, value -> draft.hud.chestplate = value);
        toggleRow("Leggings", draft.hud.leggings, value -> draft.hud.leggings = value);
        toggleRow("Boots", draft.hud.boots, value -> draft.hud.boots = value);
        toggleRow("Main Hand", draft.hud.mainHand, value -> draft.hud.mainHand = value);
        toggleRow("Off Hand", draft.hud.offhand, value -> draft.hud.offhand = value);

        sectionRow("HUD Display");
        enumRow("HUD Content", draft.hud.displayMode, DurabilityInfoConfig.HudDisplayMode.values(), value -> draft.hud.displayMode = value);
        enumRow("Layout", draft.hud.layout, HudLayout.values(), value -> draft.hud.layout = value);
        enumRow("When to Show", draft.hud.visibility, HudVisibilityMode.values(), value -> draft.hud.visibility = value);
        intRow("Low Durability Level", 0, 100, draft.hud.threshold, "%", value -> draft.hud.threshold = value);
        doubleRow("Keep Visible For", 0.5, 60, draft.hud.recentlyChangedSeconds, value -> oneDecimal(value) + " s", value -> draft.hud.recentlyChangedSeconds = value);
        toggleRow("Show Damage Used", draft.hud.showDamageTaken, value -> draft.hud.showDamageTaken = value);

        sectionRow("Position and Appearance");
        actionRow("Edit HUD Position", "Open Editor", () -> showScreen(new HudLayoutEditorScreen(this, draft)));
        enumRow("Screen Corner", draft.hud.anchor, DurabilityInfoConfig.HudAnchor.values(), value -> draft.hud.anchor = value);
        enumRow("Item Alignment", draft.hud.alignment, DurabilityInfoConfig.HudAlignment.values(), value -> draft.hud.alignment = value);
        enumRow("Armor Order", draft.hud.armorOrder, DurabilityInfoConfig.ArmorOrder.values(), value -> draft.hud.armorOrder = value);
        enumRow("Hand Order", draft.hud.handOrder, DurabilityInfoConfig.HandOrder.values(), value -> draft.hud.handOrder = value);
        doubleRow("Scale", 0.5, 2.0, draft.hud.scale, BaseConfigScreen::percentage, value -> draft.hud.scale = value);
        intRow("Spacing", 0, 20, draft.hud.spacing, " px", value -> draft.hud.spacing = value);
        toggleRow("Background", draft.hud.background, value -> draft.hud.background = value);
        doubleRow("Background Opacity", 0, 1, draft.hud.backgroundOpacity, BaseConfigScreen::percentage, value -> draft.hud.backgroundOpacity = value);
        intRow("Background Padding", 0, 12, draft.hud.backgroundPadding, " px", value -> draft.hud.backgroundPadding = value);
        toggleRow("Show Item Icons", draft.hud.showIcons, value -> draft.hud.showIcons = value);
        toggleRow("Text Shadow", draft.hud.textShadow, value -> draft.hud.textShadow = value);
        toggleRow("Snap to Guides", draft.hud.snapToGuides, value -> draft.hud.snapToGuides = value);
        intRow("Horizontal Offset", 0, 1000, draft.hud.offsetX, " px", value -> draft.hud.offsetX = value);
        intRow("Vertical Offset", 0, 1000, draft.hud.offsetY, " px", value -> draft.hud.offsetY = value);

        sectionRow("Smart Visibility");
        toggleRow("Hide in Creative Mode", draft.hud.hideCreative, value -> draft.hud.hideCreative = value);
        toggleRow("Hide in Spectator Mode", draft.hud.hideSpectator, value -> draft.hud.hideSpectator = value);
        toggleRow("Hide While Debug Screen Is Open", draft.hud.hideDebug, value -> draft.hud.hideDebug = value);
        toggleRow("Hide in Menus", draft.hud.hideInContainers, value -> draft.hud.hideInContainers = value);
        toggleRow("Follow Minecraft HUD Visibility", draft.hud.hideWhenGameHudHidden, value -> draft.hud.hideWhenGameHudHidden = value);
        toggleRow("Held Items Only", draft.hud.onlyHeld, value -> draft.hud.onlyHeld = value);
        toggleRow("Armor Only", draft.hud.onlyArmor, value -> draft.hud.onlyArmor = value);
    }

    private void warningRows() {
        sectionRow("Warning Targets");
        toggleRow("Warn for Armor", draft.alerts.armorEnabled, value -> draft.alerts.armorEnabled = value);
        toggleRow("Warn for Held Items", draft.alerts.heldEnabled, value -> draft.alerts.heldEnabled = value);
        toggleRow("Warn for Main Hand", draft.alerts.mainHandEnabled, value -> draft.alerts.mainHandEnabled = value);
        toggleRow("Warn for Off Hand", draft.alerts.offhandEnabled, value -> draft.alerts.offhandEnabled = value);
        sectionRow("Armor Warning Levels");
        thresholdRows(draft.alerts.armor);
        sectionRow("Held-Item Warning Levels");
        thresholdRows(draft.alerts.held);
        actionRow("Alert Channels", alertChannelsExpanded ? "Hide Options" : "Show Options", () -> {
            alertChannelsExpanded = !alertChannelsExpanded;
            rebuildWidgets();
        });
        if (alertChannelsExpanded) {
            toggleRow("Play Sound", draft.alerts.sound, value -> draft.alerts.sound = value);
            toggleRow("Show Above Hotbar", draft.alerts.actionBar, value -> draft.alerts.actionBar = value);
            toggleRow("Send to Chat", draft.alerts.chat, value -> draft.alerts.chat = value);
            toggleRow("Flash HUD", draft.alerts.hudFlash, value -> draft.alerts.hudFlash = value);
            doubleRow("Sound Volume", 0, 1, draft.alerts.soundVolume, BaseConfigScreen::percentage, value -> draft.alerts.soundVolume = value);
            doubleRow("Flash Duration", 0.1, 10, draft.alerts.flashSeconds, value -> oneDecimal(value) + " s", value -> draft.alerts.flashSeconds = value);
            doubleRow("Message Duration", 0.5, 15, draft.alerts.messageSeconds, value -> oneDecimal(value) + " s", value -> draft.alerts.messageSeconds = value);
        }
    }

    private void thresholdRows(AlertThresholdSet set) {
        toggleRow("Enable First Warning", set.warningEnabled, value -> set.warningEnabled = value);
        intRow("First Warning Level", 1, 100, set.warning, "%", value -> set.warning = value);
        toggleRow("Enable Low Warning", set.lowEnabled, value -> set.lowEnabled = value);
        intRow("Low Warning Level", 1, 99, set.low, "%", value -> set.low = value);
        toggleRow("Enable Critical Warning", set.criticalEnabled, value -> set.criticalEnabled = value);
        intRow("Critical Warning Level", 1, 98, set.critical, "%", value -> set.critical = value);
        toggleRow("Enable Last-Chance Warning", set.lastChanceEnabled, value -> set.lastChanceEnabled = value);
        intRow("Last-Chance Warning Level", 0, 97, set.lastChance, "%", value -> set.lastChance = value);
    }

    private void overlayRows() {
        sectionRow("Item Indicators");
        enumRow("Hotbar Item Indicators", draft.overlays.hotbar, OverlayDisplayMode.values(), value -> draft.overlays.hotbar = value);
        enumRow("Inventory Item Indicators", draft.overlays.inventory, OverlayDisplayMode.values(), value -> draft.overlays.inventory = value);
        enumRow("Container Item Indicators", draft.overlays.container, OverlayDisplayMode.values(), value -> draft.overlays.container = value);
        toggleRow("Hide at Full Durability", draft.overlays.hideFullyRepaired, value -> draft.overlays.hideFullyRepaired = value);
        toggleRow("Only Show Low Durability", draft.overlays.belowThresholdOnly, value -> draft.overlays.belowThresholdOnly = value);
        intRow("Low-Durability Level", 0, 100, draft.overlays.threshold, "%", value -> draft.overlays.threshold = value);
        sectionRow("Indicator Appearance");
        toggleRow("Text Shadow", draft.overlays.textShadow, value -> draft.overlays.textShadow = value);
        doubleRow("Indicator Scale", 0.5, 1.5, draft.overlays.scale, BaseConfigScreen::percentage, value -> draft.overlays.scale = value);
        intRow("Border Thickness", 1, 3, draft.overlays.borderThickness, " px", value -> draft.overlays.borderThickness = value);
        toggleRow("Replace Minecraft Durability Bar", draft.overlays.replaceVanillaBar, value -> draft.overlays.replaceVanillaBar = value);
    }

    private void notificationRows() {
        sectionRow("Damage and Repair Popups");
        toggleRow("Show Damage and Repair Popups", draft.notifications.enabled, value -> draft.notifications.enabled = value);
        toggleRow("Include Main Hand", draft.notifications.mainHand, value -> draft.notifications.mainHand = value);
        toggleRow("Include Off Hand", draft.notifications.offhand, value -> draft.notifications.offhand = value);
        toggleRow("Include Armor", draft.notifications.armor, value -> draft.notifications.armor = value);
        toggleRow("Include Entire Hotbar", draft.notifications.entireHotbar, value -> draft.notifications.entireHotbar = value);
        doubleRow("Popup Duration", 0.5, 10, draft.notifications.durationSeconds, value -> oneDecimal(value) + " s", value -> draft.notifications.durationSeconds = value);
        enumRow("Popup Position", draft.notifications.position, DurabilityInfoConfig.NotificationPosition.values(), value -> draft.notifications.position = value);
        doubleRow("Popup Scale", 0.5, 2, draft.notifications.scale, BaseConfigScreen::percentage, value -> draft.notifications.scale = value);
    }

    private void presetRows() {
        sectionRow("Current Setup");
        labelRow(Component.translatable("durabilityinfo.config.current_preset", presetComponent(draft.preset)));
        sectionRow("Ready-Made Setups");
        for (DurabilityPreset preset : List.of(DurabilityPreset.MINIMAL, DurabilityPreset.VANILLA_PLUS,
                DurabilityPreset.MINING, DurabilityPreset.COMBAT, DurabilityPreset.DETAILED)) {
            presetActionRow(preset);
            if (pendingPreset == preset) {
                DurabilityInfoConfig preview = preset.create();
                wrappedInfoRows(presetDescription(preset), MUTED, true);
                wrappedInfoRows(Component.translatable("durabilityinfo.config.preset.summary.tooltip",
                        valueComponent(preview.tooltip.style)), TEXT, false);
                wrappedInfoRows(Component.translatable("durabilityinfo.config.preset.summary.hud_visibility",
                        preview.hud.enabled ? valueComponent(preview.hud.visibility) : onOff(false)), TEXT, false);
                wrappedInfoRows(Component.translatable("durabilityinfo.config.preset.summary.alerts",
                        presetAlertsSummary(preview)), TEXT, false);
                wrappedInfoRows(Component.translatable("durabilityinfo.config.preset.summary.overlays",
                        valueComponent(preview.overlays.hotbar), valueComponent(preview.overlays.inventory)), TEXT, false);
                wrappedInfoRows(Component.translatable("durabilityinfo.config.preset.summary.notifications",
                        onOff(preview.notifications.enabled)), TEXT, false);
                actionRow(Component.translatable("durabilityinfo.config.confirm_preset", presetComponent(preset)),
                        Component.translatable("durabilityinfo.config.confirm"), () -> {
                        draft = preset.create();
                        pendingPreset = null;
                        rebuildWidgets();
                    });
                actionRow("Keep Current Settings", "Cancel", () -> { pendingPreset = null; rebuildWidgets(); });
            }
        }
    }

    private void presetActionRow(DurabilityPreset preset) {
        Component label = presetComponent(preset);
        int y = optionRow(label);
        if (!visible(y)) return;
        Button button = Button.builder(Component.translatable("durabilityinfo.config.preview_preset"), pressed -> {
            pendingPreset = preset;
            rebuildWidgets();
        }).pos(advancedControlX(), advancedControlY(y)).width(advancedControlWidth()).build();
        button.setTooltip(Tooltip.create(presetDescription(preset)));
        addRenderableWidget(button);
    }

    private void wrappedInfoRows(Component text, int color, boolean accent) {
        List<String> lines = wrap(text.getString(), Math.max(20, contentRight - contentLeft - 20));
        for (int index = 0; index < lines.size(); index++) {
            infoRow(Component.literal(lines.get(index)), color, accent && index == 0);
        }
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.width(candidate) > maxWidth) {
                result.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                if (!line.isEmpty()) line.append(' ');
                line.append(word);
            }
        }
        if (!line.isEmpty()) result.add(line.toString());
        if (result.isEmpty()) result.add("");
        return result;
    }

    private void infoRow(Component text, int color, boolean accent) {
        int logical = rows++;
        int y = contentTop + (logical - scroll) * rowHeight;
        if (!visible(y)) return;
        surfaces.add(new Surface(contentLeft, y + 1, contentRight - contentLeft, rowHeight - 3, accent, false));
        textLines.add(new TextLine(contentLeft + 9, y + Math.max(7, (rowHeight - 8) / 2), text, color));
    }

    private static Component presetAlertsSummary(DurabilityInfoConfig config) {
        List<AlertThresholdSet> targets = new ArrayList<>();
        if (config.alerts.armorEnabled) targets.add(config.alerts.armor);
        if (config.alerts.heldEnabled) targets.add(config.alerts.held);
        if (targets.isEmpty()) return Component.translatable("durabilityinfo.config.preset.alerts.off");

        int firstThreshold = 0;
        int maximumLevels = 0;
        boolean criticalOnly = true;
        for (AlertThresholdSet target : targets) {
            int levels = 0;
            AlertThresholdSet.Level first = null;
            for (AlertThresholdSet.Level level : target.descending()) {
                if (!level.enabled()) continue;
                levels++;
                if (first == null) first = level;
            }
            maximumLevels = Math.max(maximumLevels, levels);
            if (first != null) {
                firstThreshold = Math.max(firstThreshold, first.percentage());
                criticalOnly &= levels == 1 && "critical".equals(first.key());
            }
        }
        if (maximumLevels == 0) return Component.translatable("durabilityinfo.config.preset.alerts.off");
        if (maximumLevels > 1) {
            return Component.translatable("durabilityinfo.config.preset.alerts.multi_level", firstThreshold);
        }
        if (criticalOnly) {
            return Component.translatable("durabilityinfo.config.preset.alerts.critical_only", firstThreshold);
        }
        return Component.translatable("durabilityinfo.config.preset.alerts.single_warning", firstThreshold);
    }

    private static Component onOff(boolean enabled) {
        return Component.translatable(enabled ? "durabilityinfo.value.on" : "durabilityinfo.value.off");
    }

    private void resetVisibleSection() {
        if (!advanced) {
            SimpleSettings.resetVisibleOptions(draft);
            rebuildWidgets();
            return;
        }
        DurabilityInfoConfig defaults = ConfigDefaults.vanillaPlus();
        switch (section) {
            case TOOLTIP -> { draft.tooltip = defaults.tooltip; draft.colors = defaults.colors; customize(); }
            case HUD -> { draft.hud = defaults.hud; customize(); }
            case WARNINGS -> { draft.alerts = defaults.alerts; customize(); }
            case OVERLAYS -> { draft.overlays = defaults.overlays; customize(); }
            case NOTIFICATIONS -> { draft.notifications = defaults.notifications; customize(); }
            case PRESETS -> draft = defaults;
        }
        pendingPreset = null;
        rebuildWidgets();
    }

    private void addFooter() {
        boolean compact = panelWidth < 460;
        int gap = 4;
        if (compact) {
            int resetWidth = (panelWidth - 24) / 2;
            int resetY = footerTop + 4;
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.reset_section"), button -> resetVisibleSection())
                    .pos(panelLeft + 10, resetY).width(resetWidth).build());
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.reset_all"), button -> {
                draft = ConfigDefaults.vanillaPlus();
                pendingPreset = null;
                rebuildWidgets();
            }).pos(panelLeft + 14 + resetWidth, resetY).width(resetWidth).build());
            int actionWidth = (panelWidth - 28) / 3;
            int actionY = resetY + 23;
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.apply"), button -> apply())
                    .pos(panelLeft + 10, actionY).width(actionWidth).build());
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.cancel"), button -> closeToParent())
                    .pos(panelLeft + 14 + actionWidth, actionY).width(actionWidth).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> { if (apply()) closeToParent(); })
                    .pos(panelLeft + 18 + actionWidth * 2, actionY).width(actionWidth).build());
        } else {
            int buttonWidth = (panelWidth - 36) / 5;
            int x = panelLeft + 10;
            int y = footerTop + 7;
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.reset_section"), button -> resetVisibleSection())
                    .pos(x, y).width(buttonWidth).build());
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.reset_all"), button -> {
                draft = ConfigDefaults.vanillaPlus(); pendingPreset = null; rebuildWidgets();
            }).pos(x += buttonWidth + gap, y).width(buttonWidth).build());
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.apply"), button -> apply())
                    .pos(x += buttonWidth + gap, y).width(buttonWidth).build());
            addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.cancel"), button -> closeToParent())
                    .pos(x += buttonWidth + gap, y).width(buttonWidth).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> { if (apply()) closeToParent(); })
                    .pos(x + buttonWidth + gap, y).width(buttonWidth).build());
        }
    }

    private void addScrollButtons() {
        int y = panelTop + 6;
        int right = panelLeft + panelWidth - 10;
        Button up = Button.builder(Component.literal("↑"), button -> scrollBy(-Math.max(1, visibleRowCount() - 1)))
                .pos(right - 52, y).width(24).build();
        up.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.scroll_up")));
        up.active = scroll > 0;
        addRenderableWidget(up);
        Button down = Button.builder(Component.literal("↓"), button -> scrollBy(Math.max(1, visibleRowCount() - 1)))
                .pos(right - 24, y).width(24).build();
        down.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.scroll_down")));
        down.active = scroll < maxScroll;
        addRenderableWidget(down);
    }

    private int visibleRowCount() { return Math.max(1, (contentBottom - contentTop) / rowHeight); }

    private void scrollBy(int amount) {
        int next = Math.max(0, Math.min(maxScroll, scroll + amount));
        if (next != scroll) {
            scroll = next;
            rebuildWidgets();
        }
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xD8101118, 0xE0161821);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelBottom, 0xE51B1D25);
        graphics.outline(panelLeft, panelTop, panelWidth, panelBottom - panelTop, 0xFF3B404D);
        graphics.fill(panelLeft + 1, footerTop - 1, panelLeft + panelWidth - 1, footerTop, 0xFF343946);
        for (Surface surface : navigationHighlights) drawSurface(graphics, surface);
        for (Surface surface : surfaces) drawSurface(graphics, surface);
        for (TextLine line : textLines) graphics.text(font, line.text(), line.x(), line.y(), line.color());
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        if (advanced) {
            graphics.centeredText(font, Component.translatable("durabilityinfo.config.advanced_title"), width / 2, panelTop + 11, TEXT);
            graphics.text(font, sectionComponent(section), contentLeft + 3, sectionTitleY, TEXT);
            graphics.text(font, sectionHelp(section), contentLeft + 3, sectionTitleY + 12, MUTED);
        } else {
            graphics.centeredText(font, title, width / 2, panelTop + 8, TEXT);
            graphics.centeredText(font, Component.translatable("durabilityinfo.config.subtitle"), width / 2, panelTop + 23, MUTED);
        }
        if (maxScroll > 0) {
            graphics.text(font, Component.translatable("durabilityinfo.config.scroll_hint", scroll + 1, maxScroll + 1),
                    contentRight - 48, contentBottom - 10, MUTED);
        }
    }

    private static void drawSurface(GuiGraphicsExtractor graphics, Surface surface) {
        int color = surface.strong ? 0xD02A2E39 : 0xA0242730;
        graphics.fill(surface.x, surface.y, surface.x + surface.width, surface.y + surface.height, color);
        if (surface.accent) {
            graphics.fill(surface.x, surface.y, surface.x + 2, surface.y + surface.height, ACCENT);
            graphics.outline(surface.x, surface.y, surface.width, surface.height, 0xAA59AFFF);
        }
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX < panelLeft || mouseX > panelLeft + panelWidth || mouseY < contentTop || mouseY > contentBottom) {
            return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        int next = Math.max(0, Math.min(maxScroll, scroll + (vertical < 0 ? 1 : -1)));
        if (next != scroll) {
            scroll = next;
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case 266 -> { scrollBy(-Math.max(1, visibleRowCount() - 1)); return true; }
            case 267 -> { scrollBy(Math.max(1, visibleRowCount() - 1)); return true; }
            case 268 -> { scrollBy(-maxScroll); return true; }
            case 269 -> { scrollBy(maxScroll); return true; }
            default -> { return super.keyPressed(event); }
        }
    }

    @Override public void resize(int width, int height) {
        scroll = 0;
        super.resize(width, height);
    }

    @Override public void onClose() { closeToParent(); }
    protected abstract void closeToParent();
    protected abstract void showScreen(Screen screen);
    protected Screen parent() { return parent; }

    private void mainToggleRow(String label, String help, boolean current, Consumer<Boolean> setter) {
        int y = mainCard(label, help);
        if (!visible(y)) return;
        Component labelComponent = tr(label);
        CycleButton<Boolean> widget = CycleButton.onOffBuilder(current).displayOnlyValue()
                .create(mainControlX(), mainControlY(y), mainControlWidth(), 20, labelComponent,
                        (button, value) -> { setter.accept(value); customize(); });
        addHelp(widget, tr(help));
        addRenderableWidget(widget);
    }

    private <E extends Enum<E>> void mainEnumRow(String label, String help, E current, E[] values, Consumer<E> setter) {
        int y = mainCard(label, help);
        if (!visible(y)) return;
        Component labelComponent = tr(label);
        CycleButton<E> widget = CycleButton.builder(BaseConfigScreen::valueComponent, current)
                .withValues(values).displayOnlyValue()
                .create(mainControlX(), mainControlY(y), mainControlWidth(), 20, labelComponent,
                        (button, value) -> { setter.accept(value); customize(); });
        addHelp(widget, tr(help));
        addRenderableWidget(widget);
    }

    private void mainSliderRow(String label, String help, double min, double max, double current,
                               java.util.function.DoubleFunction<String> formatter, Consumer<Double> setter) {
        int y = mainCard(label, help);
        if (!visible(y)) return;
        ConfigSlider widget = new ConfigSlider(mainControlX(), mainControlY(y), mainControlWidth(), tr(label), false,
                min, max, current, formatter, value -> { setter.accept(value); customize(); });
        addHelp(widget, tr(help));
        addRenderableWidget(widget);
    }

    private void mainActionRow(String label, String help, String buttonLabel, Runnable action) {
        int y = mainCard(label, help);
        if (!visible(y)) return;
        Button widget = Button.builder(tr(buttonLabel), button -> action.run())
                .pos(mainControlX(), mainControlY(y)).width(mainControlWidth()).build();
        addHelp(widget, tr(help));
        addRenderableWidget(widget);
    }

    private int mainCard(String label, String help) {
        int logical = rows++;
        int y = contentTop + (logical - scroll) * rowHeight;
        if (visible(y)) {
            surfaces.add(new Surface(contentLeft, y + 2, contentRight - contentLeft, rowHeight - 5, true, false));
            boolean showHelper = panelWidth >= 430 && rowHeight >= 40;
            textLines.add(new TextLine(contentLeft + 12,
                    y + (showHelper ? 8 : Math.max(7, (rowHeight - 8) / 2)), tr(label), TEXT));
            if (showHelper) textLines.add(new TextLine(contentLeft + 12, y + 22, tr(help), MUTED));
        }
        return y;
    }

    private int mainControlWidth() {
        return Math.min(170, Math.max(108, (panelWidth - 30) / 2));
    }

    private int mainControlX() { return contentRight - mainControlWidth() - 6; }
    private int mainControlY(int rowY) { return rowY + Math.max(2, (rowHeight - 20) / 2); }

    private void sectionRow(String label) {
        int logical = rows++;
        int y = contentTop + (logical - scroll) * rowHeight;
        if (!visible(y)) return;
        surfaces.add(new Surface(contentLeft, y + 1, contentRight - contentLeft, rowHeight - 3, true, true));
        textLines.add(new TextLine(contentLeft + 10, y + Math.max(7, (rowHeight - 8) / 2), tr(label), ACCENT));
    }

    private int optionRow(Component label) {
        int logical = rows++;
        int y = contentTop + (logical - scroll) * rowHeight;
        if (visible(y)) {
            surfaces.add(new Surface(contentLeft, y + 1, contentRight - contentLeft, rowHeight - 3, false, logical % 2 == 0));
            textLines.add(new TextLine(contentLeft + 9, y + (stackAdvancedControls() ? 6 : 7), label, TEXT));
        }
        return y;
    }

    private void labelRow(Component label) { optionRow(label); }

    private boolean visible(int y) {
        return y >= contentTop && y + rowHeight - 2 <= contentBottom;
    }

    private int advancedControlWidth() {
        if (stackAdvancedControls()) return Math.max(1, contentRight - contentLeft - 12);
        return Math.min(190, Math.max(110, (panelWidth - 32) / 2));
    }

    private int advancedControlX() {
        return stackAdvancedControls() ? contentLeft + 6 : contentRight - advancedControlWidth() - 5;
    }

    private int advancedControlY(int rowY) { return rowY + (stackAdvancedControls() ? 21 : 2); }
    private boolean stackAdvancedControls() { return panelWidth < 430; }

    private void toggleRow(String label, boolean current, Consumer<Boolean> setter) {
        Component name = tr(label);
        int y = optionRow(name);
        if (!visible(y)) return;
        addRenderableWidget(CycleButton.onOffBuilder(current).displayOnlyValue()
                .create(advancedControlX(), advancedControlY(y), advancedControlWidth(), 20, name,
                        (button, value) -> { setter.accept(value); customize(); }));
    }

    private <E extends Enum<E>> void enumRow(String label, E current, E[] values, Consumer<E> setter) {
        Component name = tr(label);
        int y = optionRow(name);
        if (!visible(y)) return;
        addRenderableWidget(CycleButton.builder(BaseConfigScreen::valueComponent, current)
                .withValues(values).displayOnlyValue()
                .create(advancedControlX(), advancedControlY(y), advancedControlWidth(), 20, name,
                        (button, value) -> { setter.accept(value); customize(); }));
    }

    private void intRow(String label, int min, int max, int current, String suffix, Consumer<Integer> setter) {
        Component name = tr(label);
        int y = optionRow(name);
        if (!visible(y)) return;
        addRenderableWidget(new ConfigSlider(advancedControlX(), advancedControlY(y), advancedControlWidth(), name, false,
                min, max, current, value -> (int) Math.round(value) + suffix,
                value -> { setter.accept((int) Math.round(value)); customize(); }));
    }

    private void doubleRow(String label, double min, double max, double current,
                           java.util.function.DoubleFunction<String> formatter, Consumer<Double> setter) {
        Component name = tr(label);
        int y = optionRow(name);
        if (!visible(y)) return;
        addRenderableWidget(new ConfigSlider(advancedControlX(), advancedControlY(y), advancedControlWidth(), name, false,
                min, max, current, formatter, value -> { setter.accept(value); customize(); }));
    }

    private void actionRow(String label, String buttonLabel, Runnable action) {
        actionRow(tr(label), tr(buttonLabel), action);
    }

    private void actionRow(Component label, Component buttonLabel, Runnable action) {
        int y = optionRow(label);
        if (!visible(y)) return;
        Button button = Button.builder(buttonLabel, pressed -> action.run())
                .pos(advancedControlX(), advancedControlY(y)).width(advancedControlWidth()).build();
        button.setTooltip(Tooltip.create(label));
        addRenderableWidget(button);
    }

    private boolean apply() {
        boolean saved = DurabilityInfoConfigManager.applyAndSave(draft);
        draft = DurabilityInfoConfigManager.copyCurrent();
        pendingPreset = null;
        rebuildWidgets();
        return saved;
    }

    private void customize() { draft.preset = DurabilityPreset.CUSTOM; }

    private static <T extends AbstractWidget> T addHelp(T widget, Component help) {
        widget.setTooltip(Tooltip.create(help));
        return widget;
    }

    private static String percentage(double value) { return Math.round(value * 100) + "%"; }
    private static String oneDecimal(double value) { return String.format(Locale.ROOT, "%.1f", value); }

    private static Component sectionComponent(AdvancedSection section) {
        return Component.translatable("durabilityinfo.config.section." + section.name().toLowerCase(Locale.ROOT));
    }

    private static Component sectionHelp(AdvancedSection section) {
        return Component.translatable("durabilityinfo.config.section." + section.name().toLowerCase(Locale.ROOT) + ".help");
    }

    private static Component presetComponent(DurabilityPreset preset) {
        return Component.translatableWithFallback("durabilityinfo.preset." + preset.name().toLowerCase(Locale.ROOT), human(preset));
    }

    private static Component presetDescription(DurabilityPreset preset) {
        return Component.translatable("durabilityinfo.preset." + preset.name().toLowerCase(Locale.ROOT) + ".description");
    }

    private static String human(Enum<?> value) { return human(value.name()); }
    private static String human(String value) {
        String lower = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static Component valueComponent(Enum<?> value) {
        return Component.translatableWithFallback("durabilityinfo.value." + value.name().toLowerCase(Locale.ROOT), human(value));
    }

    private static Component tr(String fallback) {
        String key = fallback.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        return Component.translatableWithFallback("durabilityinfo.config.option." + key, fallback);
    }
}
