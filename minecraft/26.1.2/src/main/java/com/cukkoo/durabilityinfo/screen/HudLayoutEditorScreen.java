package com.cukkoo.durabilityinfo.screen;

import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import net.minecraft.client.gui.screens.Screen;

public final class HudLayoutEditorScreen extends BaseHudLayoutEditorScreen {
    public HudLayoutEditorScreen(Screen parent, DurabilityInfoConfig draft) { super(parent, draft); }
    @Override protected void closeToParent() { if (minecraft != null) minecraft.setScreen(parent()); }
}
