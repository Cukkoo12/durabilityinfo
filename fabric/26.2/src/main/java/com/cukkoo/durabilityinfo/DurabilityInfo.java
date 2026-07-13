package com.cukkoo.durabilityinfo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.cukkoo.durabilityinfo.config.ModConfig;

public class DurabilityInfo implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }

    private void onItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag type, java.util.List<Component> lines) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return;

        ModConfig config = ModConfig.load();
        if (!config.showOnUnbreakable && stack.has(DataComponents.UNBREAKABLE)) return;

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamageValue();
        int remaining = maxDamage - damage;

        if (maxDamage <= 0) return;

        int percent = (remaining * 100) / maxDamage;
        ChatFormatting color = percent > 50 ? ChatFormatting.GREEN : percent > 25 ? ChatFormatting.YELLOW : ChatFormatting.RED;

        if (config.showDurabilityNumbers) {
            lines.add(Component.translatable("durabilityinfo.tooltip.durability", remaining, maxDamage)
                    .withStyle(color));
        }

        if (config.showPercentage) {
            lines.add(Component.translatable("durabilityinfo.tooltip.percentage", percent)
                    .withStyle(color));
        }

        if (config.showBar) {
            int barLength = 20;
            int filled = (remaining * barLength) / maxDamage;

            MutableComponent bar = Component.literal("[")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("|".repeat(filled)).withStyle(color))
                    .append(Component.literal(".".repeat(barLength - filled)).withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("]"));

            lines.add(bar);
        }
    }
}
