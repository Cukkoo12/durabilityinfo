package com.cukkoo.durabilityinfo.screen;

import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.HudGeometry;
import com.cukkoo.durabilityinfo.core.HudEditorSession;
import com.cukkoo.durabilityinfo.core.HudLayout;
import com.cukkoo.durabilityinfo.core.HudPosition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public abstract class BaseHudLayoutEditorScreen extends Screen {
    private static final int ACCENT = 0xFF59AFFF;
    private static final int[] PREVIEW_COLORS = {
            0xFF55FF55, 0xFFAAFF55, 0xFFFFFF55, 0xFFFFAA55, 0xFFFF5555, 0xFF55FFAA
    };
    private static final int[] PREVIEW_PERCENTAGES = {82, 61, 43, 22, 9, 97};

    private final Screen parent;
    protected final DurabilityInfoConfig draft;
    private final HudEditorSession session;
    private boolean dragging;
    private PreviewWidget previewWidget;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelBottom;
    private int workspaceLeft;
    private int workspaceTop;
    private int workspaceRight;
    private int workspaceBottom;
    private int footerTop;

    protected BaseHudLayoutEditorScreen(Screen parent, DurabilityInfoConfig draft) {
        super(Component.translatable("durabilityinfo.config.hud_editor"));
        this.parent = parent;
        this.draft = draft;
        this.session = new HudEditorSession(draft);
    }

    @Override protected void init() {
        calculateFrame();
        int controlTop = panelTop + 34;
        boolean compact = panelWidth < 520;
        int columns = compact ? 2 : 4;
        int gap = 4;
        int controlWidth = (panelWidth - 20 - (columns - 1) * gap) / columns;

        addControl(0, columns, controlTop, controlWidth, gap, layoutButton(controlWidth));

        ConfigSlider scale = new ConfigSlider(0, 0, controlWidth,
                Component.translatable("durabilityinfo.config.option.scale"), true,
                0.5, 2.0, draft.hud.scale, BaseHudLayoutEditorScreen::percentage, value -> {
                    draft.hud.scale = value;
                    markCustom();
                    previewWidget.syncBounds();
                });
        scale.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.hud_editor.scale_help")));
        addControl(1, columns, controlTop, controlWidth, gap, scale);

        CycleButton<Boolean> background = CycleButton.onOffBuilder(draft.hud.background)
                .create(0, 0, controlWidth, 20, Component.translatable("durabilityinfo.config.option.background"),
                        (button, value) -> { draft.hud.background = value; markCustom(); previewWidget.syncBounds(); });
        background.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.hud_editor.background_help")));
        addControl(2, columns, controlTop, controlWidth, gap, background);

        Button reset = Button.builder(Component.translatable("durabilityinfo.config.reset_position"), button -> {
            draft.hud.anchor = DurabilityInfoConfig.HudAnchor.BOTTOM_RIGHT;
            draft.hud.offsetX = 4;
            draft.hud.offsetY = 4;
            markCustom();
            previewWidget.syncBounds();
        }).width(controlWidth).build();
        reset.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.hud_editor.reset_help")));
        addControl(3, columns, controlTop, controlWidth, gap, reset);

        previewWidget = new PreviewWidget();
        previewWidget.syncBounds();
        addRenderableWidget(previewWidget);

        int footerGap = 4;
        int footerButtonWidth = (panelWidth - 28) / 3;
        int footerY = footerTop + 7;
        addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.apply"), button -> apply())
                .pos(panelLeft + 10, footerY).width(footerButtonWidth).build());
        addRenderableWidget(Button.builder(Component.translatable("durabilityinfo.config.cancel"), button -> cancel())
                .pos(panelLeft + 14 + footerButtonWidth, footerY).width(footerButtonWidth).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> { if (apply()) closeToParent(); })
                .pos(panelLeft + 18 + footerButtonWidth * 2, footerY).width(footerButtonWidth).build());
    }

    private void calculateFrame() {
        panelWidth = Math.max(1, Math.min(620, width - 16));
        panelLeft = (width - panelWidth) / 2;
        panelTop = 8;
        panelBottom = Math.max(panelTop + 1, height - 8);
        footerTop = panelBottom - 34;
        boolean compact = panelWidth < 520;
        int controlRows = compact ? 2 : 1;
        workspaceLeft = panelLeft + 10;
        workspaceRight = panelLeft + panelWidth - 10;
        workspaceTop = panelTop + 34 + controlRows * 24 + 8;
        workspaceBottom = Math.max(workspaceTop + 1, footerTop - 18);
    }

    private Button layoutButton(int width) {
        Component message = CommonComponents.optionNameValue(
                Component.translatable("durabilityinfo.config.option.layout"), valueComponent(draft.hud.layout));
        Button button = Button.builder(message, pressed -> {
            draft.hud.layout = draft.hud.layout == HudLayout.VERTICAL ? HudLayout.HORIZONTAL : HudLayout.VERTICAL;
            markCustom();
            rebuildWidgets();
        }).width(width).build();
        button.setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.hud_editor.layout_help")));
        return button;
    }

    private void addControl(int index, int columns, int top, int controlWidth, int gap, AbstractWidget widget) {
        int column = index % columns;
        int row = index / columns;
        widget.setX(panelLeft + 10 + column * (controlWidth + gap));
        widget.setY(top + row * 24);
        addRenderableWidget(widget);
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xD8101118, 0xE0161821);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelBottom, 0xE51B1D25);
        graphics.outline(panelLeft, panelTop, panelWidth, panelBottom - panelTop, 0xFF3B404D);
        graphics.fill(workspaceLeft, workspaceTop, workspaceRight, workspaceBottom, 0xB00D0F14);
        graphics.outline(workspaceLeft, workspaceTop, workspaceRight - workspaceLeft,
                workspaceBottom - workspaceTop, 0xFF3B5166);
        int viewportLeft = viewportLeft();
        int viewportTop = viewportTop();
        int viewportRight = viewportLeft + viewportWidth();
        int viewportBottom = viewportTop + viewportHeight();
        graphics.outline(viewportLeft, viewportTop, viewportWidth(), viewportHeight(), 0x6659AFFF);
        graphics.verticalLine((viewportLeft + viewportRight) / 2, viewportTop + 1, viewportBottom - 1, 0x3359AFFF);
        graphics.horizontalLine(viewportLeft + 1, viewportRight - 1,
                (viewportTop + viewportBottom) / 2, 0x3359AFFF);
        graphics.fill(panelLeft + 1, footerTop - 1, panelLeft + panelWidth - 1, footerTop, 0xFF343946);
        previewWidget.syncBounds();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, panelTop + 9, 0xFFF2F2F2);
        graphics.centeredText(font, Component.translatable("durabilityinfo.config.hud_editor.help"),
                width / 2, workspaceBottom + 5, 0xFFADAFB8);
    }

    private void drawPreview(GuiGraphicsExtractor graphics) {
        HudGeometry.Layout geometry = previewLayout();
        double visualScale = visualScale();
        int previewWidth = previewWidth();
        int previewHeight = previewHeight();
        HudPosition position = position();

        graphics.pose().pushMatrix();
        graphics.pose().translate(position.x(), position.y());
        graphics.pose().scale((float) visualScale, (float) visualScale);
        if (draft.hud.background) {
            int alpha = (int) Math.round(draft.hud.backgroundOpacity * 255.0) << 24;
            HudGeometry.Rect panel = geometry.panelBounds();
            graphics.fill(panel.x(), panel.y(), panel.right(), panel.bottom(), alpha | 0x00101010);
        }

        for (int index = 0; index < PREVIEW_PERCENTAGES.length; index++) {
            HudGeometry.Row row = geometry.row(index);
            if (draft.hud.showIcons) {
                HudGeometry.Rect item = row.itemBox();
                graphics.fill(item.x(), item.y(), item.right(), item.bottom(), 0x442B303A);
                graphics.fill(item.x() + 3, item.y() + 3, item.right() - 3, item.bottom() - 3,
                        PREVIEW_COLORS[index]);
            }
            String text = previewText(index);
            HudGeometry.ValueGeometry value = HudGeometry.value(row, draft.hud.showIcons,
                    draft.hud.displayMode, draft.hud.alignment, font.width(text), font.lineHeight);
            if (value.hasText() && !text.isEmpty()) {
                HudGeometry.Rect textBounds = value.textBounds();
                graphics.text(font, text, textBounds.x(), textBounds.y(), PREVIEW_COLORS[index], draft.hud.textShadow);
            }
            if (value.hasBar()) {
                HudGeometry.Rect bar = value.barBounds();
                graphics.fill(bar.x(), bar.y(), bar.right(), bar.bottom(), 0xFF333333);
                int filled = PREVIEW_PERCENTAGES[index] * bar.width() / 100;
                if (filled > 0) {
                    graphics.fill(bar.x(), bar.y(), bar.x() + filled, bar.bottom(), PREVIEW_COLORS[index]);
                }
            }
        }
        graphics.pose().popMatrix();
        graphics.outline(position.x(), position.y(), previewWidth, previewHeight, dragging ? 0xFFFFFF66 : ACCENT);
    }

    @Override public void onClose() { cancel(); }
    protected abstract void closeToParent();
    protected Screen parent() { return parent; }

    private boolean apply() {
        return session.applyAndSave();
    }

    private void cancel() {
        session.cancel();
        closeToParent();
    }

    private void markCustom() {
        session.markChanged();
    }

    private HudPosition position() {
        HudGeometry.Size actual = previewLayout().scaledPanelSize(draft.hud.scale);
        int previewWidth = previewWidth();
        int previewHeight = previewHeight();
        HudPosition actualPosition = HudPosition.fromAnchor(draft.hud.anchor, draft.hud.offsetX, draft.hud.offsetY,
                actual.width(), actual.height(), width, height);
        int rawX = viewportLeft() + (int) Math.round(actualPosition.x() * viewportScale());
        int rawY = viewportTop() + (int) Math.round(actualPosition.y() * viewportScale());
        return clampToViewport(rawX, rawY, previewWidth, previewHeight);
    }

    private HudPosition clampToViewport(int rawX, int rawY, int previewWidth, int previewHeight) {
        int margin = viewportMargin();
        int minX = viewportLeft() + margin;
        int minY = viewportTop() + margin;
        int maxX = Math.max(minX, viewportLeft() + viewportWidth() - previewWidth - margin);
        int maxY = Math.max(minY, viewportTop() + viewportHeight() - previewHeight - margin);
        return new HudPosition(Math.max(minX, Math.min(maxX, rawX)), Math.max(minY, Math.min(maxY, rawY)));
    }

    private void setAbsolute(int rawX, int rawY, boolean userChange) {
        int previewWidth = previewWidth();
        int previewHeight = previewHeight();
        int outerWidth = previewWidth;
        int outerHeight = previewHeight;
        HudPosition clamped = clampToViewport(rawX, rawY, outerWidth, outerHeight);
        int outerX = clamped.x();
        int outerY = clamped.y();
        if (draft.hud.snapToGuides) {
            int margin = viewportMargin();
            int tolerance = Math.max(2, (int) Math.round(6 * viewportScale()));
            int left = viewportLeft() + margin;
            int right = viewportLeft() + viewportWidth() - outerWidth - margin;
            int top = viewportTop() + margin;
            int bottom = viewportTop() + viewportHeight() - outerHeight - margin;
            if (Math.abs(outerX - left) <= tolerance) outerX = left;
            if (Math.abs((outerX + outerWidth / 2) - (viewportLeft() + viewportWidth() / 2)) <= tolerance) {
                outerX = viewportLeft() + (viewportWidth() - outerWidth) / 2;
            }
            if (Math.abs(outerX - right) <= tolerance) outerX = right;
            if (Math.abs(outerY - top) <= tolerance) outerY = top;
            if (Math.abs((outerY + outerHeight / 2) - (viewportTop() + viewportHeight() / 2)) <= tolerance) {
                outerY = viewportTop() + (viewportHeight() - outerHeight) / 2;
            }
            if (Math.abs(outerY - bottom) <= tolerance) outerY = bottom;
        }

        HudGeometry.Size actual = previewLayout().scaledPanelSize(draft.hud.scale);
        int actualX = (int) Math.round((outerX - viewportLeft()) / viewportScale());
        int actualY = (int) Math.round((outerY - viewportTop()) / viewportScale());
        draft.hud.offsetX = switch (draft.hud.anchor) {
            case TOP_RIGHT, BOTTOM_RIGHT -> Math.max(0, width - actual.width() - actualX);
            default -> Math.max(0, actualX);
        };
        draft.hud.offsetY = switch (draft.hud.anchor) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> Math.max(0, height - actual.height() - actualY);
            default -> Math.max(0, actualY);
        };
        if (userChange) markCustom();
    }

    private void nudge(int deltaX, int deltaY) {
        HudGeometry.Size actual = previewLayout().scaledPanelSize(draft.hud.scale);
        HudPosition current = HudPosition.fromAnchor(draft.hud.anchor, draft.hud.offsetX, draft.hud.offsetY,
                actual.width(), actual.height(), width, height);
        int visualX = viewportLeft() + (int) Math.round((current.x() + deltaX) * viewportScale());
        int visualY = viewportTop() + (int) Math.round((current.y() + deltaY) * viewportScale());
        setAbsolute(visualX, visualY, true);
    }

    private double visualScale() {
        HudGeometry.Size base = previewLayout().panelSize();
        double desired = draft.hud.scale * viewportScale();
        double fit = Math.min(
                Math.max(1, viewportWidth() - 6) / (double) Math.max(1, base.width()),
                Math.max(1, viewportHeight() - 6) / (double) Math.max(1, base.height()));
        return Math.min(desired, fit);
    }

    private double viewportScale() {
        int availableWidth = Math.max(1, workspaceRight - workspaceLeft - 12);
        int availableHeight = Math.max(1, workspaceBottom - workspaceTop - 12);
        return Math.max(0.01, Math.min(availableWidth / (double) Math.max(1, width),
                availableHeight / (double) Math.max(1, height)));
    }

    private int viewportWidth() { return Math.max(1, (int) Math.round(width * viewportScale())); }
    private int viewportHeight() { return Math.max(1, (int) Math.round(height * viewportScale())); }
    private int viewportLeft() { return (workspaceLeft + workspaceRight - viewportWidth()) / 2; }
    private int viewportTop() { return (workspaceTop + workspaceBottom - viewportHeight()) / 2; }
    private int viewportMargin() { return Math.max(1, (int) Math.round(4 * viewportScale())); }

    private int previewWidth() {
        HudGeometry.Size base = previewLayout().panelSize();
        return Math.max(1, (int) Math.round(base.width() * visualScale()));
    }

    private int previewHeight() {
        HudGeometry.Size base = previewLayout().panelSize();
        return Math.max(1, (int) Math.round(base.height() * visualScale()));
    }

    private HudGeometry.Layout previewLayout() {
        int padding = draft.hud.background ? draft.hud.backgroundPadding : 0;
        return HudGeometry.calculate(draft.hud.layout, PREVIEW_PERCENTAGES.length, draft.hud.spacing, padding);
    }

    private String previewText(int index) {
        int percentage = PREVIEW_PERCENTAGES[index];
        return switch (draft.hud.displayMode) {
            case MINI_BAR -> "";
            case PERCENTAGE -> percentage + "%";
            case REMAINING -> Integer.toString(percentage);
            case REMAINING_AND_MAX -> percentage + "/100";
            case COMBINED -> percentage + "/100 " + percentage + "%";
        };
    }

    private final class PreviewWidget extends AbstractWidget {
        private PreviewWidget() {
            super(0, 0, 1, 1, Component.translatable("durabilityinfo.config.hud_editor.preview"));
            setTooltip(Tooltip.create(Component.translatable("durabilityinfo.config.hud_editor.preview_help")));
        }

        private void syncBounds() {
            HudPosition position = position();
            setRectangle(previewWidth(), previewHeight(), position.x(), position.y());
            setMessage(Component.translatable("durabilityinfo.config.hud_editor.preview_narration",
                    valueComponent(draft.hud.anchor), draft.hud.offsetX, draft.hud.offsetY));
        }

        @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            drawPreview(graphics);
        }

        @Override protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override public void onClick(MouseButtonEvent event, boolean doubleClick) {
            dragging = true;
        }

        @Override public void onRelease(MouseButtonEvent event) {
            dragging = false;
        }

        @Override protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            HudPosition current = position();
            setAbsolute((int) Math.round(current.x() + dragX), (int) Math.round(current.y() + dragY), true);
            syncBounds();
        }

        @Override public boolean keyPressed(KeyEvent event) {
            int step = (event.modifiers() & 1) != 0 ? 10 : 1;
            switch (event.key()) {
                case 262 -> nudge(step, 0);
                case 263 -> nudge(-step, 0);
                case 264 -> nudge(0, step);
                case 265 -> nudge(0, -step);
                default -> { return false; }
            }
            syncBounds();
            return true;
        }
    }

    private static String percentage(double value) { return Math.round(value * 100) + "%"; }

    private static Component valueComponent(Enum<?> value) {
        String key = value.name().toLowerCase(Locale.ROOT);
        String lower = value.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        String fallback = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        return Component.translatableWithFallback("durabilityinfo.value." + key, fallback);
    }
}
