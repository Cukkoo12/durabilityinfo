package com.cukkoo.durabilityinfo.screen;

import com.cukkoo.durabilityinfo.config.ModConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private final ModConfig backup;

    public ModConfigScreen(Screen parent) {
        super(Component.translatable("durabilityinfo.config"));
        this.parent = parent;
        this.config = ModConfig.load();
        this.backup = ModConfig.load();
    }

    @Override
    protected void init() {
        int y = 35;
        int w = 210;
        int x = width / 2 - w / 2;

        addRenderableWidget(toggleBtn(x, y, w, "showDurabilityNumbers", config.showDurabilityNumbers, v -> config.showDurabilityNumbers = v));
        y += 22;
        addRenderableWidget(toggleBtn(x, y, w, "showPercentage", config.showPercentage, v -> config.showPercentage = v));
        y += 22;
        addRenderableWidget(toggleBtn(x, y, w, "showBar", config.showBar, v -> config.showBar = v));
        y += 22;
        addRenderableWidget(toggleBtn(x, y, w, "showOnUnbreakable", config.showOnUnbreakable, v -> config.showOnUnbreakable = v));
        y += 22;
        addRenderableWidget(toggleBtn(x, y, w, "showDamageDealt", config.showDamageDealt, v -> config.showDamageDealt = v));

        // Done / Cancel
        int btnY = height - 30;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            config.save();
            onClose();
        }).pos(width / 2 - 105, btnY).width(100).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
            config.showDurabilityNumbers = backup.showDurabilityNumbers;
            config.showPercentage = backup.showPercentage;
            config.showBar = backup.showBar;
            config.showOnUnbreakable = backup.showOnUnbreakable;
            config.showDamageDealt = backup.showDamageDealt;
            config.hudDisplayMode = backup.hudDisplayMode;
            config.hudAnchor = backup.hudAnchor;
            config.warningThreshold = backup.warningThreshold;
            config.hudOffsetX = backup.hudOffsetX;
            config.hudOffsetY = backup.hudOffsetY;
            onClose();
        }).pos(width / 2 + 5, btnY).width(100).build());
    }

    private Button toggleBtn(int x, int y, int w, String key, boolean value, java.util.function.Consumer<Boolean> setter) {
        String label = Component.translatable("durabilityinfo.config." + key).getString() + ": " + (value ? "ON" : "OFF");
        return Button.builder(Component.literal(label), b -> {
            setter.accept(!value);
            rebuildWidgets();
        }).pos(x, y).width(w).build();
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }
}
