package com.cukkoo.durabilityinfo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudGeometryTest {
    private static final int SLOTS = 6;
    private static final int SPACING = 3;
    private static final int PADDING = 4;

    @Test void sixVerticalRowsHaveFixedSizeAndEqualGaps() {
        HudGeometry.Layout layout = HudGeometry.calculate(HudLayout.VERTICAL, SLOTS, SPACING, PADDING);
        assertEquals(1, layout.columns());
        assertEquals(6, layout.rows());
        assertEquals(new HudGeometry.Rect(4, 4, 42, 147), layout.contentBounds());
        for (int index = 0; index < SLOTS; index++) {
            HudGeometry.Rect bounds = layout.row(index).bounds();
            assertEquals(HudGeometry.SLOT_WIDTH, bounds.width());
            assertEquals(HudGeometry.SLOT_HEIGHT, bounds.height());
            assertEquals(4 + index * (HudGeometry.SLOT_HEIGHT + SPACING), bounds.y());
            if (index > 0) {
                assertEquals(SPACING, bounds.y() - layout.row(index - 1).bounds().bottom());
            }
        }
    }

    @Test void everyVerticalRowSharesIconValueAndBarColumns() {
        HudGeometry.Layout layout = HudGeometry.calculate(HudLayout.VERTICAL, SLOTS, SPACING, PADDING);
        Integer iconRelativeX = null;
        Integer itemRelativeY = null;
        Integer valueRelativeX = null;
        Integer barRelativeX = null;
        Integer barRelativeY = null;
        for (int index = 0; index < SLOTS; index++) {
            HudGeometry.Row row = layout.row(index);
            HudGeometry.ValueGeometry value = HudGeometry.value(row, true,
                    DurabilityInfoConfig.HudDisplayMode.COMBINED,
                    DurabilityInfoConfig.HudAlignment.CENTER, 8 + index * 11, 9);
            int nextIconX = row.iconBox().x() - row.bounds().x();
            int nextItemY = row.itemBox().y() - row.bounds().y();
            int nextValueX = value.valueArea().x() - row.bounds().x();
            int nextBarX = value.barBounds().x() - row.bounds().x();
            int nextBarY = value.barBounds().y() - row.bounds().y();
            if (iconRelativeX == null) {
                iconRelativeX = nextIconX;
                itemRelativeY = nextItemY;
                valueRelativeX = nextValueX;
                barRelativeX = nextBarX;
                barRelativeY = nextBarY;
            }
            assertEquals(iconRelativeX.intValue(), nextIconX);
            assertEquals(itemRelativeY.intValue(), nextItemY);
            assertEquals(valueRelativeX.intValue(), nextValueX);
            assertEquals(barRelativeX.intValue(), nextBarX);
            assertEquals(barRelativeY.intValue(), nextBarY);
        }
    }

    @Test void itemRenderBoxIsCenteredInsideFixedIconBox() {
        HudGeometry.Row row = HudGeometry.calculate(HudLayout.VERTICAL, 1, SPACING, PADDING).row(0);
        assertEquals(new HudGeometry.Rect(4, 4, 16, 22), row.iconBox());
        assertEquals(new HudGeometry.Rect(4, 7, 16, 16), row.itemBox());
        assertEquals(row.iconBox().x() + (row.iconBox().width() - row.itemBox().width()) / 2, row.itemBox().x());
        assertEquals(row.iconBox().y() + (row.iconBox().height() - row.itemBox().height()) / 2, row.itemBox().y());
    }

    @Test void barBoundsNeverDependOnTextWidth() {
        HudGeometry.Row row = HudGeometry.calculate(HudLayout.VERTICAL, 1, SPACING, PADDING).row(0);
        HudGeometry.ValueGeometry shortText = HudGeometry.value(row, true,
                DurabilityInfoConfig.HudDisplayMode.COMBINED,
                DurabilityInfoConfig.HudAlignment.END, 6, 9);
        HudGeometry.ValueGeometry longText = HudGeometry.value(row, true,
                DurabilityInfoConfig.HudDisplayMode.COMBINED,
                DurabilityInfoConfig.HudAlignment.END, 100, 9);
        assertEquals(shortText.barBounds(), longText.barBounds());
        assertEquals(HudGeometry.BAR_WIDTH, shortText.barBounds().width());
        assertEquals(HudGeometry.BAR_HEIGHT, shortText.barBounds().height());
        assertTrue(longText.textBounds().x() >= row.valueArea(true).x());
    }

    @Test void backgroundPaddingIsBalancedAroundFinalContent() {
        for (HudLayout type : HudLayout.values()) {
            HudGeometry.Layout layout = HudGeometry.calculate(type, SLOTS, SPACING, PADDING);
            HudGeometry.Rect panel = layout.panelBounds();
            HudGeometry.Rect content = layout.contentBounds();
            assertEquals(PADDING, content.x() - panel.x());
            assertEquals(PADDING, content.y() - panel.y());
            assertEquals(PADDING, panel.right() - content.right());
            assertEquals(PADDING, panel.bottom() - content.bottom());
        }
    }

    @Test void allLayoutsKeepTheirContentGridAndScaleTheWholePanel() {
        HudGeometry.Layout vertical = HudGeometry.calculate(HudLayout.VERTICAL, SLOTS, SPACING, PADDING);
        HudGeometry.Layout horizontal = HudGeometry.calculate(HudLayout.HORIZONTAL, SLOTS, SPACING, PADDING);
        HudGeometry.Layout grid = HudGeometry.calculate(HudLayout.COMPACT_GRID, SLOTS, SPACING, PADDING);

        assertEquals(new HudGeometry.Rect(4, 4, 42, 147), vertical.contentBounds());
        assertEquals(new HudGeometry.Rect(4, 4, 267, 22), horizontal.contentBounds());
        assertEquals(new HudGeometry.Rect(4, 4, 87, 72), grid.contentBounds());
        assertEquals(225, horizontal.row(5).bounds().x() - PADDING);
        assertEquals(45, grid.row(5).bounds().x() - PADDING);
        assertEquals(50, grid.row(5).bounds().y() - PADDING);

        assertEquals(new HudGeometry.Size(25, 78), vertical.scaledPanelSize(0.5));
        assertEquals(new HudGeometry.Size(43, 132), vertical.scaledPanelSize(0.85));
        assertEquals(new HudGeometry.Size(50, 155), vertical.scaledPanelSize(1.0));
        assertEquals(new HudGeometry.Size(100, 310), vertical.scaledPanelSize(2.0));
        assertEquals(new HudGeometry.Size(275, 30), horizontal.scaledPanelSize(1.0));
        assertEquals(new HudGeometry.Size(95, 80), grid.scaledPanelSize(1.0));
        assertEquals(new HudGeometry.Size(234, 26), horizontal.scaledPanelSize(0.85));
        assertEquals(new HudGeometry.Size(81, 68), grid.scaledPanelSize(0.85));
    }

    @Test void differentGuiSizesKeepTheWholePanelInsideTheSameAnchorMargin() {
        HudGeometry.Size panel = HudGeometry.calculate(HudLayout.VERTICAL, SLOTS, SPACING, PADDING)
                .scaledPanelSize(1.0);
        HudPosition small = HudPosition.fromAnchor(DurabilityInfoConfig.HudAnchor.BOTTOM_RIGHT,
                4, 4, panel.width(), panel.height(), 320, 240);
        HudPosition large = HudPosition.fromAnchor(DurabilityInfoConfig.HudAnchor.BOTTOM_RIGHT,
                4, 4, panel.width(), panel.height(), 640, 480);
        assertEquals(4, 320 - panel.width() - small.x());
        assertEquals(4, 240 - panel.height() - small.y());
        assertEquals(4, 640 - panel.width() - large.x());
        assertEquals(4, 480 - panel.height() - large.y());
    }
}
