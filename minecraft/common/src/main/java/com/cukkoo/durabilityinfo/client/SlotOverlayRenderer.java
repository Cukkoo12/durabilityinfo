package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.DurabilityCalculator;
import com.cukkoo.durabilityinfo.core.DurabilityColorScale;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.core.OverlayDecision;
import com.cukkoo.durabilityinfo.core.OverlayDisplayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class SlotOverlayRenderer {
    private SlotOverlayRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, OverlayDisplayMode mode) {
        if (mode == OverlayDisplayMode.OFF) return;
        DurabilityInfoConfig config = DurabilityInfoConfigManager.current();
        var snapshot = MinecraftDurabilityAdapter.snapshot("overlay", stack);
        if (!OverlayDecision.shouldRender(snapshot, mode, config.overlays)) return;
        int percentage = DurabilityCalculator.percentage(snapshot);
        int color = DurabilityColorScale.argb(percentage, config.colors);
        float scale = (float) config.overlays.scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        switch (mode) {
            case PERCENTAGE -> graphics.text(Minecraft.getInstance().font, percentage + "%", 0, 0,
                    color, config.overlays.textShadow);
            case REMAINING -> graphics.text(Minecraft.getInstance().font,
                    Integer.toString(DurabilityCalculator.remaining(snapshot)), 0, 0,
                    color, config.overlays.textShadow);
            case MINI_BAR -> {
                graphics.fill(0, 0, 16, 2, 0xD0333333);
                int filled = percentage * 16 / 100;
                if (filled > 0) graphics.fill(0, 0, filled, 2, color);
            }
            case COLORED_BORDER -> {
                int thickness = config.overlays.borderThickness;
                graphics.fill(0, 0, 16, thickness, color);
                graphics.fill(0, 16 - thickness, 16, 16, color);
                graphics.fill(0, 0, thickness, 16, color);
                graphics.fill(16 - thickness, 0, 16, 16, color);
            }
            case OFF -> { }
        }
        graphics.pose().popMatrix();
    }
}
