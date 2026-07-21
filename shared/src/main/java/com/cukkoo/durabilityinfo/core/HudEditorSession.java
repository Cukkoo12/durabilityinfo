package com.cukkoo.durabilityinfo.core;

import java.util.Objects;

/** Transaction boundary for the nested HUD editor. */
public final class HudEditorSession {
    private final DurabilityInfoConfig parentDraft;
    private DurabilityInfoConfig.HudConfig baselineHud;
    private DurabilityPreset baselinePreset;

    public HudEditorSession(DurabilityInfoConfig parentDraft) {
        this.parentDraft = Objects.requireNonNull(parentDraft, "parentDraft");
        this.baselineHud = DurabilityInfoConfigManager.copyHud(parentDraft.hud);
        this.baselinePreset = parentDraft.preset;
    }

    public void markChanged() {
        parentDraft.preset = DurabilityPreset.CUSTOM;
    }

    /** Saves only HUD-owned fields; unrelated parent-screen edits remain unapplied. */
    public boolean applyAndSave() {
        DurabilityInfoConfig persisted = DurabilityInfoConfigManager.copyCurrent();
        boolean hudChanged = !DurabilityInfoConfigManager.hudEquals(persisted.hud, parentDraft.hud);
        persisted.hud = DurabilityInfoConfigManager.copyHud(parentDraft.hud);
        if (hudChanged) persisted.preset = DurabilityPreset.CUSTOM;
        boolean saved = DurabilityInfoConfigManager.applyAndSave(persisted);
        if (saved) {
            baselineHud = DurabilityInfoConfigManager.copyHud(parentDraft.hud);
            baselinePreset = parentDraft.preset;
        }
        return saved;
    }

    /** Restores changes made after opening the editor or after its most recent Apply. */
    public void cancel() {
        parentDraft.hud = DurabilityInfoConfigManager.copyHud(baselineHud);
        parentDraft.preset = baselinePreset;
    }
}
