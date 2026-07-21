package com.cukkoo.durabilityinfo;

import com.cukkoo.durabilityinfo.client.TooltipRenderer;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.screen.ModConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

final class DurabilityInfoClient {
    private DurabilityInfoClient() {}
    static void initialize(ModContainer container) {
        DurabilityInfoConfigManager.initialize(FMLPaths.CONFIGDIR.get(), System.err::println);
        container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new ModConfigScreen(parent));
    }
    @EventBusSubscriber(modid = DurabilityInfo.MOD_ID, value = Dist.CLIENT)
    static final class Events {
        @SubscribeEvent static void tooltip(ItemTooltipEvent event) { TooltipRenderer.append(event.getItemStack(), event.getToolTip()); }
    }
}
