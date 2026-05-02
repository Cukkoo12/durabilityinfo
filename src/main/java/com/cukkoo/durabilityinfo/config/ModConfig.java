package com.cukkoo.durabilityinfo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("durabilityinfo.json");

    public boolean showDurabilityNumbers = true;
    public boolean showPercentage = true;
    public boolean showBar = true;
    public boolean showOnUnbreakable = false;

    public static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH))
                return new Gson().fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
        } catch (IOException e) { /* use defaults */ }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH,
                    new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) { /* ignore */ }
    }
}
