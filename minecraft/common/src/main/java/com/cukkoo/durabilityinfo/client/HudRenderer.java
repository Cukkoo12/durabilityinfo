package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.core.HudGeometry;
import com.cukkoo.durabilityinfo.core.HudPosition;
import com.cukkoo.durabilityinfo.core.HudVisibilityDecider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudRenderer {
    private static final String[] KEYS = {"helmet", "chestplate", "leggings", "boots", "main_hand", "offhand"};
    private static final int[] ORDER = new int[6];
    private static final int[] VISIBLE = new int[6];

    private HudRenderer() {}

    public static void render(GuiGraphicsExtractor graphics) {
        DurabilityInfoConfig config = DurabilityInfoConfigManager.current();
        if (!config.hud.enabled) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (config.hud.hideCreative && client.player.isCreative()) return;
        if (config.hud.hideSpectator && client.player.isSpectator()) return;
        if (config.hud.hideInContainers && ClientUiState.hasScreen(client)) return;
        if (config.hud.hideDebug && ClientUiState.debugVisible(client)) return;

        order(config.hud);
        long now = System.currentTimeMillis();
        int visible = 0;
        for (int orderIndex = 0; orderIndex < ORDER.length; orderIndex++) {
            int i = ORDER[orderIndex];
            HudSlotView view = ClientRuntime.view(i);
            if (enabled(i, config.hud) && HudVisibilityDecider.shouldShow(
                    view.snapshot(), config.hud, now, ClientRuntime.changedAt(KEYS[i]))) VISIBLE[visible++] = i;
        }
        if (visible == 0) return;

        int padding = config.hud.background ? config.hud.backgroundPadding : 0;
        HudGeometry.Layout geometry = HudGeometry.calculate(config.hud.layout, visible, config.hud.spacing, padding);
        float scale = (float) config.hud.scale;
        HudGeometry.Size scaledPanel = geometry.scaledPanelSize(scale);
        HudPosition position = HudPosition.fromAnchor(config.hud.anchor, config.hud.offsetX, config.hud.offsetY,
                scaledPanel.width(), scaledPanel.height(), graphics.guiWidth(), graphics.guiHeight());

        graphics.pose().pushMatrix();
        graphics.pose().translate(position.x(), position.y());
        graphics.pose().scale(scale, scale);
        if (config.hud.background) {
            int alpha = (int) Math.round(config.hud.backgroundOpacity * 255.0) << 24;
            HudGeometry.Rect panel = geometry.panelBounds();
            graphics.fill(panel.x(), panel.y(), panel.right(), panel.bottom(), alpha | 0x00101010);
        }

        for (int rendered = 0; rendered < visible; rendered++) {
            int i = VISIBLE[rendered];
            HudSlotView view = ClientRuntime.view(i);
            renderSlot(graphics, client.font, view, geometry.row(rendered), config, now);
        }
        graphics.pose().popMatrix();
    }

    private static boolean enabled(int index, DurabilityInfoConfig.HudConfig hud) {
        if (hud.onlyHeld && index < 4) return false;
        if (hud.onlyArmor && index >= 4) return false;
        return switch (index) {
            case 0 -> hud.helmet;
            case 1 -> hud.chestplate;
            case 2 -> hud.leggings;
            case 3 -> hud.boots;
            case 4 -> hud.mainHand;
            case 5 -> hud.offhand;
            default -> false;
        };
    }

    private static void order(DurabilityInfoConfig.HudConfig hud) {
        if (hud.armorOrder == DurabilityInfoConfig.ArmorOrder.HEAD_TO_FEET) {
            ORDER[0] = 0; ORDER[1] = 1; ORDER[2] = 2; ORDER[3] = 3;
        } else {
            ORDER[0] = 3; ORDER[1] = 2; ORDER[2] = 1; ORDER[3] = 0;
        }
        if (hud.handOrder == DurabilityInfoConfig.HandOrder.MAIN_THEN_OFFHAND) {
            ORDER[4] = 4; ORDER[5] = 5;
        } else {
            ORDER[4] = 5; ORDER[5] = 4;
        }
    }

    private static void renderSlot(GuiGraphicsExtractor graphics, Font font, HudSlotView view,
                                   HudGeometry.Row row, DurabilityInfoConfig config, long now) {
        int percentage = view.percentage();
        int color = ClientRuntime.isFlashing(view.snapshot().slotKey(), now)
                ? 0xFFFF2020 : view.color();
        if (config.hud.showIcons) {
            HudGeometry.Rect item = row.itemBox();
            graphics.item(view.stack(), item.x(), item.y());
        }
        String text = view.text(config.hud.displayMode);
        HudGeometry.ValueGeometry value = HudGeometry.value(row, config.hud.showIcons,
                config.hud.displayMode, config.hud.alignment, font.width(text), font.lineHeight);
        if (value.hasText() && !text.isEmpty()) {
            HudGeometry.Rect textBounds = value.textBounds();
            graphics.text(font, text, textBounds.x(), textBounds.y(), color, config.hud.textShadow);
        }
        if (value.hasBar()) {
            HudGeometry.Rect bar = value.barBounds();
            graphics.fill(bar.x(), bar.y(), bar.right(), bar.bottom(), 0xFF333333);
            int filled = percentage * bar.width() / 100;
            if (filled > 0) graphics.fill(bar.x(), bar.y(), bar.x() + filled, bar.bottom(), color);
        }
    }
}
