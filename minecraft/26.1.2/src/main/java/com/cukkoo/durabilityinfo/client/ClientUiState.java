package com.cukkoo.durabilityinfo.client;

import net.minecraft.client.Minecraft;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.OverlayDisplayMode;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class ClientUiState {
    private ClientUiState() {}
    public static boolean hasScreen(Minecraft client) { return client.screen != null; }
    public static boolean debugVisible(Minecraft client) { return client.getDebugOverlay().showDebugScreen(); }
    public static OverlayDisplayMode containerOverlayMode(Minecraft client, DurabilityInfoConfig config) {
        return client.screen instanceof InventoryScreen ? config.overlays.inventory : config.overlays.container;
    }
}
