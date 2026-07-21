package com.cukkoo.durabilityinfo;

import com.cukkoo.durabilityinfo.client.TooltipRenderer;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.screen.ModConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

final class DurabilityInfoClient {
    private DurabilityInfoClient() {}
    static void initialize() {
        DurabilityInfoConfigManager.initialize(FMLPaths.CONFIGDIR.get(), System.err::println);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ModConfigScreen(parent)));
    }
    @Mod.EventBusSubscriber(modid = DurabilityInfo.MOD_ID, value = Dist.CLIENT)
    static final class Events {
        @SubscribeEvent static void tooltip(ItemTooltipEvent event) { TooltipRenderer.append(event.getItemStack(), event.getToolTip()); }
    }
}
