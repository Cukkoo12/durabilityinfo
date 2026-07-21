package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.DurabilitySnapshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class MinecraftDurabilityAdapter {
    private MinecraftDurabilityAdapter() {}

    public static DurabilitySnapshot snapshot(String slotKey, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return DurabilitySnapshot.empty(slotKey);
        return new DurabilitySnapshot(
                slotKey,
                stack.getItem().toString(),
                stack.getHoverName().getString(),
                stack.getMaxDamage(),
                stack.getDamageValue(),
                stack.isDamageableItem(),
                stack.has(DataComponents.UNBREAKABLE),
                false);
    }
}
