package com.cukkoo.durabilityinfo;

import com.cukkoo.durabilityinfo.client.TooltipRenderer;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class DurabilityInfo implements ClientModInitializer {
    @Override public void onInitializeClient() {
        DurabilityInfoConfigManager.initialize(FabricLoader.getInstance().getConfigDir(), System.err::println);
        ItemTooltipCallback.EVENT.register(this::appendTooltip);
    }

    private void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag,
                               java.util.List<net.minecraft.network.chat.Component> lines) {
        TooltipRenderer.append(stack, lines);
    }
}
