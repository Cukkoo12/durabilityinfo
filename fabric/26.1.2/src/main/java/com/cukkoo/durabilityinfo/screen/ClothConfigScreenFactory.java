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

        addTooltipOptions(category, entryBuilder, config);
        addHudOptions(category, entryBuilder, config);

        builder.setSavingRunnable(config::save);
        return builder.build();
    }

    private static void addTooltipOptions(ConfigCategory category, ConfigEntryBuilder eb, ModConfig config) {
        category.addEntry(eb.startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showDurabilityNumbers"),
                        config.showDurabilityNumbers)
                .setDefaultValue(true)
                .setSaveConsumer(v -> config.showDurabilityNumbers = v)
                .build());

        category.addEntry(eb.startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showPercentage"),
                        config.showPercentage)
                .setDefaultValue(true)
                .setSaveConsumer(v -> config.showPercentage = v)
                .build());

        category.addEntry(eb.startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showBar"),
                        config.showBar)
                .setDefaultValue(true)
                .setSaveConsumer(v -> config.showBar = v)
                .build());

        category.addEntry(eb.startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showOnUnbreakable"),
                        config.showOnUnbreakable)
                .setDefaultValue(false)
                .setSaveConsumer(v -> config.showOnUnbreakable = v)
                .build());
    }

    private static void addHudOptions(ConfigCategory category, ConfigEntryBuilder eb, ModConfig config) {
        category.addEntry(eb.startIntSlider(
                        Component.translatable("durabilityinfo.config.warningThreshold"),
                        config.warningThreshold, 0, 100)
                .setDefaultValue(10)
                .setSaveConsumer(v -> config.warningThreshold = v)
                .build());

        category.addEntry(eb.startEnumSelector(
                        Component.translatable("durabilityinfo.config.hudAnchor"),
                        ModConfig.HudAnchor.class,
                        config.hudAnchor)
                .setDefaultValue(ModConfig.HudAnchor.BOTTOM_RIGHT)
                .setSaveConsumer(v -> config.hudAnchor = v)
                .build());

        category.addEntry(eb.startEnumSelector(
                        Component.translatable("durabilityinfo.config.hudDisplayMode"),
                        ModConfig.HudDisplayMode.class,
                        config.hudDisplayMode)
                .setDefaultValue(ModConfig.HudDisplayMode.BAR)
                .setSaveConsumer(v -> config.hudDisplayMode = v)
                .build());

        category.addEntry(eb.startIntField(
                        Component.translatable("durabilityinfo.config.hudOffsetX"),
                        config.hudOffsetX)
                .setDefaultValue(4)
                .setSaveConsumer(v -> config.hudOffsetX = v)
                .build());

        category.addEntry(eb.startIntField(
                        Component.translatable("durabilityinfo.config.hudOffsetY"),
                        config.hudOffsetY)
                .setDefaultValue(4)
                .setSaveConsumer(v -> config.hudOffsetY = v)
                .build());

        category.addEntry(eb.startBooleanToggle(
                        Component.translatable("durabilityinfo.config.showDamageDealt"),
                        config.showDamageDealt)
                .setDefaultValue(false)
                .setSaveConsumer(v -> config.showDamageDealt = v)
                .build());
    }
}
