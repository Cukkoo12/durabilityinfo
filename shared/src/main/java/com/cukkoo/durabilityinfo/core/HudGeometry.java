package com.cukkoo.durabilityinfo.core;

import java.util.Objects;

/** Fixed HUD geometry shared by the live renderer and layout editor. */
public final class HudGeometry {
    public static final int SLOT_WIDTH = 42;
    public static final int SLOT_HEIGHT = 22;
    public static final int ITEM_RENDER_SIZE = 16;
    public static final int ICON_BOX_WIDTH = 16;
    public static final int ICON_VALUE_GAP = 2;
    public static final int BAR_WIDTH = 22;
    public static final int BAR_HEIGHT = 3;
    public static final int VALUE_STACK_GAP = 2;

    private HudGeometry() {}

    public static Layout calculate(HudLayout layout, int visibleSlots, int spacing, int panelPadding) {
        Objects.requireNonNull(layout, "layout");
        int slots = Math.max(0, visibleSlots);
        int gap = Math.max(0, spacing);
        int padding = Math.max(0, panelPadding);
        int columns = slots == 0 ? 0 : switch (layout) {
            case VERTICAL -> 1;
            case HORIZONTAL -> slots;
            case COMPACT_GRID -> Math.min(2, slots);
        };
        int rows = columns == 0 ? 0 : (slots + columns - 1) / columns;
        int contentWidth = columns == 0 ? 0 : columns * SLOT_WIDTH + (columns - 1) * gap;
        int contentHeight = rows == 0 ? 0 : rows * SLOT_HEIGHT + (rows - 1) * gap;
        Rect content = new Rect(padding, padding, contentWidth, contentHeight);
        Rect panel = new Rect(0, 0, contentWidth + padding * 2, contentHeight + padding * 2);
        return new Layout(layout, slots, gap, padding, columns, rows, content, panel);
    }

    /** Content size without optional background padding. */
    public static Size unscaled(HudLayout layout, int visibleSlots, int spacing) {
        Rect content = calculate(layout, visibleSlots, spacing, 0).contentBounds();
        return new Size(content.width(), content.height());
    }

    /** Scaled content size without optional background padding. */
    public static Size scaled(HudLayout layout, int visibleSlots, int spacing, double scale) {
        return scale(unscaled(layout, visibleSlots, spacing), scale);
    }

    public static Size scale(Size size, double scale) {
        Objects.requireNonNull(size, "size");
        if (!Double.isFinite(scale) || scale < 0.0) throw new IllegalArgumentException("scale must be finite and non-negative");
        return new Size(scaledDimension(size.width(), scale), scaledDimension(size.height(), scale));
    }

    public static ValueGeometry value(Row row, boolean showIcons,
                                      DurabilityInfoConfig.HudDisplayMode mode,
                                      DurabilityInfoConfig.HudAlignment alignment,
                                      int textWidth, int fontHeight) {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(alignment, "alignment");
        Rect area = row.valueArea(showIcons);
        int safeFontHeight = Math.max(1, fontHeight);
        int fittedTextWidth = Math.min(area.width(), Math.max(0, textWidth));
        int textX = alignedX(area, fittedTextWidth, alignment);
        boolean hasText = mode != DurabilityInfoConfig.HudDisplayMode.MINI_BAR;
        boolean hasBar = mode == DurabilityInfoConfig.HudDisplayMode.MINI_BAR
                || mode == DurabilityInfoConfig.HudDisplayMode.COMBINED;
        int barWidth = Math.min(BAR_WIDTH, area.width());
        int barX = alignedX(area, barWidth, alignment);
        int textY;
        int barY;
        if (hasText && hasBar) {
            int groupHeight = safeFontHeight + VALUE_STACK_GAP + BAR_HEIGHT;
            int top = area.y() + Math.max(0, (area.height() - groupHeight) / 2);
            textY = top;
            barY = top + safeFontHeight + VALUE_STACK_GAP;
        } else if (hasText) {
            textY = area.y() + Math.max(0, (area.height() - safeFontHeight) / 2);
            barY = area.y();
        } else {
            textY = area.y();
            barY = area.y() + Math.max(0, (area.height() - BAR_HEIGHT) / 2);
        }
        Rect textBounds = new Rect(textX, textY, fittedTextWidth, safeFontHeight);
        Rect barBounds = new Rect(barX, barY, barWidth, BAR_HEIGHT);
        return new ValueGeometry(area, textBounds, barBounds, hasText, hasBar);
    }

    private static int alignedX(Rect area, int width, DurabilityInfoConfig.HudAlignment alignment) {
        return switch (alignment) {
            case START -> area.x();
            case CENTER -> area.x() + Math.max(0, (area.width() - width) / 2);
            case END -> area.x() + Math.max(0, area.width() - width);
        };
    }

    private static int scaledDimension(int value, double scale) {
        return value == 0 ? 0 : Math.max(1, (int) Math.round(value * scale));
    }

    public record Layout(HudLayout type, int visibleSlots, int spacing, int padding,
                         int columns, int rows, Rect contentBounds, Rect panelBounds) {
        public Row row(int slotIndex) {
            if (slotIndex < 0 || slotIndex >= visibleSlots) throw new IndexOutOfBoundsException(slotIndex);
            int column = slotIndex % columns;
            int row = slotIndex / columns;
            int x = contentBounds.x() + column * (SLOT_WIDTH + spacing);
            int y = contentBounds.y() + row * (SLOT_HEIGHT + spacing);
            Rect bounds = new Rect(x, y, SLOT_WIDTH, SLOT_HEIGHT);
            Rect iconBox = new Rect(x, y, ICON_BOX_WIDTH, SLOT_HEIGHT);
            Rect itemBox = new Rect(
                    iconBox.x() + (iconBox.width() - ITEM_RENDER_SIZE) / 2,
                    iconBox.y() + (iconBox.height() - ITEM_RENDER_SIZE) / 2,
                    ITEM_RENDER_SIZE, ITEM_RENDER_SIZE);
            Rect withIcon = new Rect(x + ICON_BOX_WIDTH + ICON_VALUE_GAP, y,
                    SLOT_WIDTH - ICON_BOX_WIDTH - ICON_VALUE_GAP, SLOT_HEIGHT);
            Rect withoutIcon = new Rect(x, y, SLOT_WIDTH, SLOT_HEIGHT);
            return new Row(slotIndex, row, column, bounds, iconBox, itemBox, withIcon, withoutIcon);
        }

        public Size panelSize() { return new Size(panelBounds.width(), panelBounds.height()); }
        public Size scaledPanelSize(double scale) { return HudGeometry.scale(panelSize(), scale); }
    }

    public record Row(int index, int row, int column, Rect bounds, Rect iconBox, Rect itemBox,
                      Rect valueAreaWithIcon, Rect valueAreaWithoutIcon) {
        public Rect valueArea(boolean showIcons) {
            return showIcons ? valueAreaWithIcon : valueAreaWithoutIcon;
        }
    }

    public record ValueGeometry(Rect valueArea, Rect textBounds, Rect barBounds,
                                boolean hasText, boolean hasBar) {}

    public record Rect(int x, int y, int width, int height) {
        public Rect {
            if (width < 0 || height < 0) throw new IllegalArgumentException("negative rectangle size");
        }
        public int right() { return x + width; }
        public int bottom() { return y + height; }
    }

    public record Size(int width, int height) {}
}
