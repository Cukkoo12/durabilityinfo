package com.cukkoo.durabilityinfo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("durabilityinfo.json");

    public boolean showDurabilityNumbers = true;
    public boolean showPercentage = true;
    public boolean showBar = true;
    public boolean showOnUnbreakable = false;
    public int warningThreshold = 10;
    public HudAnchor hudAnchor = HudAnchor.BOTTOM_RIGHT;
    public int hudOffsetX = 4;
    public int hudOffsetY = 4;
    public boolean showDamageDealt = false;
    public HudDisplayMode hudDisplayMode = HudDisplayMode.BAR;

    public enum HudAnchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum HudDisplayMode { BAR, PERCENTAGE }

    public static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH))
                return new Gson().fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
        } catch (IOException e) { }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) { }
    }
}
