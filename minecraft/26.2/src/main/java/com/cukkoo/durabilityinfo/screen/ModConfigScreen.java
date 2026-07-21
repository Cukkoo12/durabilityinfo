package com.cukkoo.durabilityinfo.screen;

import net.minecraft.client.gui.screens.Screen;

public final class ModConfigScreen extends BaseConfigScreen {
    public ModConfigScreen(Screen parent) { super(parent); }
    @Override protected void closeToParent() { if (minecraft != null) minecraft.gui.setScreen(parent()); }
    @Override protected void showScreen(Screen screen) { if (minecraft != null) minecraft.gui.setScreen(screen); }
}
