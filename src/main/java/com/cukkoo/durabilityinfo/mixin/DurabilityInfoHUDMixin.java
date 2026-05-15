package com.cukkoo.durabilityinfo.mixin;

import com.cukkoo.durabilityinfo.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Gui.class)
public class DurabilityInfoHUDMixin {

    private static final int SLOT_W = 18;
    private static final int ICON_S = 16;
    private static final int BAR_H = 3;
    private static final int GAP = 3;

    @Unique
    private final Map<String, Boolean> durabilityinfo$wasLow = new HashMap<>();

    @Unique
    private long durabilityinfo$lastWarningTime = 0;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN"))
    private void onExtractRenderState(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (client.options.hideGui) return;

        ModConfig config = ModConfig.load();
        int sw = extractor.guiWidth();
        int sh = extractor.guiHeight();
        Font font = client.font;

        List<SlotEntry> slots = buildSlots(client);
        if (slots.isEmpty()) return;

        boolean isPct = config.hudDisplayMode == ModConfig.HudDisplayMode.PERCENTAGE;
        int slotH = ICON_S + 1 + (isPct ? font.lineHeight : BAR_H) + GAP;
        int totalH = slots.size() * slotH;

        int x = switch (config.hudAnchor) {
            case TOP_RIGHT, BOTTOM_RIGHT -> sw - SLOT_W - config.hudOffsetX;
            default -> config.hudOffsetX;
        };
        int y = switch (config.hudAnchor) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> sh - totalH - config.hudOffsetY;
            default -> config.hudOffsetY;
        };

        // Background
        extractor.fill(x - 1, y - 1, x + SLOT_W + 1, y + totalH + 1, 0x66000000);

        long now = System.currentTimeMillis();
        for (int i = 0; i < slots.size(); i++) {
            SlotEntry e = slots.get(i);
            ItemStack stack = e.stack();
            int sy = y + i * slotH;
            int ix = x + (SLOT_W - ICON_S) / 2;

            int maxDmg = stack.getMaxDamage();
            int dmg = stack.getDamageValue();
            int remain = maxDmg - dmg;
            int pct = maxDmg > 0 ? (remain * 100) / maxDmg : 100;
            boolean warn = pct <= config.warningThreshold;

            // Item icon
            extractor.item(stack, ix, sy);

            if (isPct) {
                // Percentage / damage text under icon
                String txt = config.showDamageDealt ? dmg + "d" : pct + "%";
                int tw = font.width(txt);
                extractor.text(font, txt, x + (SLOT_W - tw) / 2, sy + ICON_S + 1,
                        warn ? flashColor(now) : colorForPct(pct));
            } else {
                // Durability bar under icon
                int by = sy + ICON_S + 1;
                int fill = (pct * SLOT_W) / 100;
                int barC = warn ? flashColor(now) : colorForPct(pct);

                extractor.fill(x, by, x + SLOT_W, by + BAR_H, 0xFF333333);
                if (fill > 0) {
                    extractor.fill(x, by, x + fill, by + BAR_H, barC);
                }
            }

            // Sound on transition to low durability
            boolean was = durabilityinfo$wasLow.getOrDefault(e.key(), false);
            if (warn && !was && now - durabilityinfo$lastWarningTime > 2000) {
                client.player.playSound(SoundEvents.ITEM_BREAK.value(), 1.0F, 1.0F);
                durabilityinfo$lastWarningTime = now;
            }
            durabilityinfo$wasLow.put(e.key(), warn);
        }
    }

    @Unique
    private static List<SlotEntry> buildSlots(Minecraft client) {
        List<SlotEntry> list = new ArrayList<>();
        addIfDmg(list, "head", client.player.getItemBySlot(EquipmentSlot.HEAD));
        addIfDmg(list, "chest", client.player.getItemBySlot(EquipmentSlot.CHEST));
        addIfDmg(list, "legs", client.player.getItemBySlot(EquipmentSlot.LEGS));
        addIfDmg(list, "feet", client.player.getItemBySlot(EquipmentSlot.FEET));
        addIfDmg(list, "main", client.player.getMainHandItem());
        addIfDmg(list, "off", client.player.getItemBySlot(EquipmentSlot.OFFHAND));
        return list;
    }

    @Unique
    private static void addIfDmg(List<SlotEntry> list, String key, ItemStack stack) {
        if (stack.isDamageableItem()) list.add(new SlotEntry(stack, key));
    }

    @Unique
    private static int colorForPct(int pct) {
        if (pct > 50) return 0xFF55FF55;
        if (pct > 25) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    @Unique
    private static int flashColor(long now) {
        return (now / 400 % 2 == 0) ? 0xFFFF0000 : 0x40FF0000;
    }

    @Unique
    private record SlotEntry(ItemStack stack, String key) {}
}
