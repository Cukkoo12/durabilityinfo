package com.cukkoo.durabilityinfo.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

public final class DurabilityInfoConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter CORRUPT_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static volatile DurabilityInfoConfig current = ConfigDefaults.vanillaPlus();
    private static Path path;
    private static Consumer<String> logger = message -> {};
    private static boolean initialized;
    private static boolean loadErrorLogged;
    private static boolean saveErrorLogged;
    private static long diskReadCount;

    private DurabilityInfoConfigManager() {}

    public static synchronized void initialize(Path configDirectory, Consumer<String> logSink) {
        if (initialized) return;
        path = Objects.requireNonNull(configDirectory, "configDirectory").resolve("durabilityinfo.json");
        logger = logSink == null ? message -> {} : logSink;
        LoadResult loaded = loadFromDisk();
        current = loaded.config();
        initialized = true;
        if (loaded.needsSave()) save();
    }

    public static DurabilityInfoConfig current() {
        return current;
    }

    public static DurabilityInfoConfig copyCurrent() {
        return copy(current);
    }

    public static DurabilityInfoConfig copyOf(DurabilityInfoConfig config) {
        return copy(Objects.requireNonNull(config, "config"));
    }

    public static DurabilityInfoConfig.HudConfig copyHud(DurabilityInfoConfig.HudConfig hud) {
        Objects.requireNonNull(hud, "hud");
        return GSON.fromJson(GSON.toJson(hud), DurabilityInfoConfig.HudConfig.class);
    }

    public static boolean hudEquals(DurabilityInfoConfig.HudConfig first, DurabilityInfoConfig.HudConfig second) {
        return GSON.toJson(Objects.requireNonNull(first, "first"))
                .equals(GSON.toJson(Objects.requireNonNull(second, "second")));
    }

    public static synchronized void apply(DurabilityInfoConfig draft) {
        current = copy(ConfigValidator.validate(copy(draft)));
    }

    public static synchronized boolean applyAndSave(DurabilityInfoConfig draft) {
        apply(draft);
        return save();
    }

    public static synchronized boolean save() {
        ensureInitialized();
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temporary, GSON.toJson(current) + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException ex) {
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) {}
            logSaveFailureOnce(ex);
            return false;
        }
    }

    public static long diskReadCountForTests() {
        return diskReadCount;
    }

    static synchronized void resetForTests() {
        initialized = false;
        path = null;
        current = ConfigDefaults.vanillaPlus();
        logger = message -> {};
        loadErrorLogged = false;
        saveErrorLogged = false;
        diskReadCount = 0;
    }

    private static LoadResult loadFromDisk() {
        try {
            if (Files.notExists(path)) {
                return new LoadResult(ConfigValidator.validate(ConfigDefaults.vanillaPlus()), true);
            }
            diskReadCount++;
            JsonObject source = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            boolean migrated = !source.has("schemaVersion")
                    || source.get("schemaVersion").getAsInt() < DurabilityInfoConfig.CURRENT_SCHEMA;
            return new LoadResult(ConfigMigration.migrate(source, GSON), migrated);
        } catch (IOException | RuntimeException ex) {
            boolean backedUp = backupCorruptFile();
            logLoadFailureOnce(ex);
            return new LoadResult(ConfigValidator.validate(ConfigDefaults.vanillaPlus()), backedUp);
        }
    }

    private static boolean backupCorruptFile() {
        try {
            String stem = path.getFileName() + "." + CORRUPT_STAMP.format(LocalDateTime.now());
            Path backup = path.resolveSibling(stem + ".corrupt");
            int suffix = 1;
            while (Files.exists(backup)) backup = path.resolveSibling(stem + "-" + suffix++ + ".corrupt");
            Files.move(path, backup);
            logger.accept("Backed up malformed DurabilityInfo config to " + backup);
            return true;
        } catch (IOException | RuntimeException backupFailure) {
            logger.accept("Could not back up malformed DurabilityInfo config: " + backupFailure.getMessage());
            return false;
        }
    }

    private static DurabilityInfoConfig copy(DurabilityInfoConfig config) {
        return GSON.fromJson(GSON.toJson(config), DurabilityInfoConfig.class);
    }

    private static void ensureInitialized() {
        if (!initialized || path == null) throw new IllegalStateException("DurabilityInfo config manager is not initialized");
    }

    private static void logLoadFailureOnce(Exception ex) {
        if (!loadErrorLogged) {
            loadErrorLogged = true;
            logger.accept("Failed to load DurabilityInfo config; using safe defaults: " + ex.getMessage());
        }
    }

    private static void logSaveFailureOnce(Exception ex) {
        if (!saveErrorLogged) {
            saveErrorLogged = true;
            logger.accept("Failed to save DurabilityInfo config: " + ex.getMessage());
        }
    }

    private record LoadResult(DurabilityInfoConfig config, boolean needsSave) {}
}
