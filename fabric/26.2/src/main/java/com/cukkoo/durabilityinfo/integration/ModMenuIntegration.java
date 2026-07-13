package com.cukkoo.durabilityinfo.integration;

import com.cukkoo.durabilityinfo.screen.ClothConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            return parent -> ClothConfigScreenFactory.create(parent);
        }
        return null;
    }
}
