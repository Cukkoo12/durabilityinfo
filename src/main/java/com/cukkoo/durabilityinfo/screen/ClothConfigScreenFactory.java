package com.cukkoo.durabilityinfo.screen;

import com.cukkoo.durabilityinfo.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreenFactory {

    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.load();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("durabilityinfo.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(
                Component.translatable("durabilityinfo.config.category"));

        category.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showDurabilityNumbers"),
                        config.showDurabilityNumbers)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.showDurabilityNumbers = val)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showPercentage"),
                        config.showPercentage)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.showPercentage = val)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showBar"),
                        config.showBar)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.showBar = val)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showOnUnbreakable"),
                        config.showOnUnbreakable)
                .setDefaultValue(false)
                .setSaveConsumer(val -> config.showOnUnbreakable = val)
                .build());

        builder.setSavingRunnable(config::save);

        return builder.build();
    }
}
