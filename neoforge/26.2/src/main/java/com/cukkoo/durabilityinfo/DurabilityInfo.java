package com.cukkoo.durabilityinfo;

import com.cukkoo.durabilityinfo.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import com.cukkoo.durabilityinfo.screen.ModConfigScreen;

@Mod(DurabilityInfo.MOD_ID)
public class DurabilityInfo {
    public static final String MOD_ID = "durabilityinfo";

    public DurabilityInfo(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (mc, screen) -> new ModConfigScreen(screen)
        );
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class TooltipHandler {
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty() || !stack.isDamageableItem()) return;

            ModConfig config = ModConfig.load();
            if (!config.showOnUnbreakable && stack.has(DataComponents.UNBREAKABLE)) return;

            int maxDamage = stack.getMaxDamage();
            int damage = stack.getDamageValue();
            int remaining = maxDamage - damage;

            if (maxDamage <= 0) return;

            int percent = (remaining * 100) / maxDamage;
            ChatFormatting color = percent > 50 ? ChatFormatting.GREEN : percent > 25 ? ChatFormatting.YELLOW : ChatFormatting.RED;

            java.util.List<Component> lines = event.getToolTip();

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
}
