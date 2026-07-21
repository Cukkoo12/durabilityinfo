package com.cukkoo.durabilityinfo.core;

import java.util.ArrayList;
import java.util.List;

public final class TooltipFormatter {
    private TooltipFormatter() {}

    public static List<TooltipLine> format(DurabilitySnapshot snapshot, DurabilityInfoConfig config) {
        TooltipStyle style = config.tooltip.style;
        if (style == TooltipStyle.OFF || !DurabilityCalculator.isUsable(snapshot)) return List.of();
        if (snapshot.unbreakable()) {
            return config.tooltip.showUnbreakable
                    ? List.of(TooltipLine.translatable("durabilityinfo.tooltip.unbreakable", DurabilityColorScale.Band.HEALTHY))
                    : List.of();
        }
        int remaining = DurabilityCalculator.remaining(snapshot);
        if (config.tooltip.hideFullyRepaired && remaining == snapshot.maxDurability()) return List.of();
        int value = config.tooltip.showDamageTaken ? DurabilityCalculator.damageTaken(snapshot) : remaining;
        int percentage = DurabilityCalculator.percentage(snapshot);
        DurabilityColorScale.Band band = DurabilityColorScale.band(percentage, config.colors);
        String numbers = value + " / " + snapshot.maxDurability();
        String percent = percentage + "%";
        String bar = bar(percentage, config.tooltip.barWidth);
        return switch (style) {
            case COMPACT -> List.of(TooltipLine.translatable(
                    "durabilityinfo.tooltip.compact", band, value, snapshot.maxDurability(), percentage));
            case VANILLA_PLUS -> config.tooltip.showBar
                    ? List.of(TooltipLine.translatable("durabilityinfo.tooltip.vanilla_plus", band,
                            value, snapshot.maxDurability(), percentage), TooltipLine.literal(bar, band))
                    : List.of(TooltipLine.translatable("durabilityinfo.tooltip.vanilla_plus", band,
                            value, snapshot.maxDurability(), percentage));
            case DETAILED -> List.of(TooltipLine.translatable("durabilityinfo.tooltip.detailed_remaining", band,
                            value, snapshot.maxDurability()),
                    TooltipLine.translatable("durabilityinfo.tooltip.detailed_percentage", band, percentage),
                    TooltipLine.literal(bar, band));
            case BAR_ONLY -> List.of(TooltipLine.literal(bar, band));
            case CUSTOM -> custom(config, numbers, percent, bar, band);
            case OFF -> List.of();
        };
    }

    private static List<TooltipLine> custom(DurabilityInfoConfig config, String numbers, String percent,
                                            String bar, DurabilityColorScale.Band band) {
        List<TooltipLine> lines = new ArrayList<>(3);
        if (config.tooltip.showNumbers) lines.add(TooltipLine.translatable(
                "durabilityinfo.tooltip.durability", band, numbers.split(" / ")[0], numbers.split(" / ")[1]));
        if (config.tooltip.showPercentage) lines.add(TooltipLine.translatable(
                "durabilityinfo.tooltip.percentage", band, percent.substring(0, percent.length() - 1)));
        if (config.tooltip.showBar) lines.add(TooltipLine.literal(bar, band));
        return List.copyOf(lines);
    }

    static String bar(int percentage, int width) {
        int safeWidth = DurabilityCalculator.clamp(width, 5, 60);
        int filled = (percentage * safeWidth) / 100;
        return "[" + "|".repeat(filled) + ".".repeat(safeWidth - filled) + "]";
    }

    public record TooltipLine(String text, String translationKey, Object[] arguments, DurabilityColorScale.Band band) {
        static TooltipLine literal(String text, DurabilityColorScale.Band band) {
            return new TooltipLine(text, null, new Object[0], band);
        }
        static TooltipLine translatable(String key, DurabilityColorScale.Band band, Object... arguments) {
            return new TooltipLine("", key, arguments, band);
        }
    }
}
