package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.DurabilityColorScale;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.core.TooltipFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TooltipRenderer {
    private TooltipRenderer() {}

    public static void append(ItemStack stack, List<Component> lines) {
        var config = DurabilityInfoConfigManager.current();
        if (config.tooltip.style == com.cukkoo.durabilityinfo.core.TooltipStyle.OFF) return;
        for (TooltipFormatter.TooltipLine line : TooltipFormatter.format(
                MinecraftDurabilityAdapter.snapshot("tooltip", stack), config)) {
            Component component = line.translationKey() == null
                    ? Component.literal(line.text())
                    : Component.translatable(line.translationKey(), line.arguments());
            lines.add(component.copy().withStyle(format(line.band())));
        }
    }

    private static ChatFormatting format(DurabilityColorScale.Band band) {
        return switch (band) {
            case HEALTHY -> ChatFormatting.GREEN;
            case WORN -> ChatFormatting.GOLD;
            case LOW, CRITICAL -> ChatFormatting.RED;
        };
    }
}
