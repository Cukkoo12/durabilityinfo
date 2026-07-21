package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.DurabilityCalculator;
import com.cukkoo.durabilityinfo.core.DurabilityColorScale;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.DurabilitySnapshot;
import net.minecraft.world.item.ItemStack;

public final class HudSlotView {
    private ItemStack stack = ItemStack.EMPTY;
    private DurabilitySnapshot snapshot = DurabilitySnapshot.empty("");
    private int percentage;
    private int color;
    private String percentageText = "";
    private String remainingText = "";
    private String remainingAndMaxText = "";
    private String combinedText = "";
    private String itemKey = "";
    private int damage = Integer.MIN_VALUE;
    private int maximum = Integer.MIN_VALUE;
    private DurabilityInfoConfig configIdentity;

    public void update(String slotKey, ItemStack current, DurabilityInfoConfig config) {
        stack = current == null ? ItemStack.EMPTY : current;
        String nextItem = stack.isEmpty() ? "" : stack.getItem().toString();
        int nextDamage = stack.isEmpty() ? -1 : stack.getDamageValue();
        int nextMaximum = stack.isEmpty() ? 0 : stack.getMaxDamage();
        if (nextItem.equals(itemKey) && nextDamage == damage && nextMaximum == maximum && configIdentity == config) return;
        itemKey = nextItem;
        damage = nextDamage;
        maximum = nextMaximum;
        configIdentity = config;
        snapshot = MinecraftDurabilityAdapter.snapshot(slotKey, stack);
        percentage = DurabilityCalculator.percentage(snapshot);
        color = DurabilityColorScale.argb(percentage, config.colors);
        int shown = config.hud.showDamageTaken ? DurabilityCalculator.damageTaken(snapshot) : DurabilityCalculator.remaining(snapshot);
        percentageText = percentage + "%";
        remainingText = Integer.toString(shown);
        remainingAndMaxText = shown + "/" + snapshot.maxDurability();
        combinedText = DurabilityCalculator.remaining(snapshot) + "/" + snapshot.maxDurability() + " " + percentageText;
    }

    public ItemStack stack() { return stack; }
    public DurabilitySnapshot snapshot() { return snapshot; }
    public int percentage() { return percentage; }
    public int color() { return color; }
    public String text(DurabilityInfoConfig.HudDisplayMode mode) {
        return switch (mode) {
            case PERCENTAGE -> percentageText;
            case REMAINING -> remainingText;
            case REMAINING_AND_MAX -> remainingAndMaxText;
            case COMBINED -> combinedText;
            case MINI_BAR -> "";
        };
    }
}
