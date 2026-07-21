package com.cukkoo.durabilityinfo.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
    @TempDir Path temporary;

    @AfterEach void reset() { DurabilityInfoConfigManager.resetForTests(); }

    @Test void missingFileCreatesValidatedPrettyPrintedDefaultsWithoutRenderLoopReads() throws Exception {
        Path configDirectory = temporary.resolve("nested/config");
        DurabilityInfoConfigManager.initialize(configDirectory, message -> {});
        assertEquals(DurabilityPreset.VANILLA_PLUS, DurabilityInfoConfigManager.current().preset);
        assertEquals(TooltipStyle.VANILLA_PLUS, DurabilityInfoConfigManager.current().tooltip.style);
        assertEquals(10, DurabilityInfoConfigManager.current().alerts.armor.warning);
        assertEquals(HudVisibilityMode.ALWAYS, DurabilityInfoConfigManager.current().hud.visibility);
        assertEquals(0.85, DurabilityInfoConfigManager.current().hud.scale);
        Path config = configDirectory.resolve("durabilityinfo.json");
        assertTrue(Files.isDirectory(configDirectory));
        assertTrue(Files.exists(config));
        String json = Files.readString(config);
        assertTrue(json.contains("\n  \"schemaVersion\": 2"));
        assertTrue(json.contains("\"tooltip\""));
        assertTrue(json.contains("\"hud\""));
        assertTrue(json.contains("\"alerts\""));
        assertTrue(json.contains("\"notifications\""));
        assertTrue(json.contains("\"overlays\""));
        assertTrue(json.contains("\"colors\""));
        assertTrue(json.contains("\"visibility\": \"ALWAYS\""));
        assertTrue(json.contains("\"scale\": 0.85"));
        assertFalse(Files.exists(configDirectory.resolve("durabilityinfo.json.tmp")));
        long reads = DurabilityInfoConfigManager.diskReadCountForTests();
        for (int i = 0; i < 1000; i++) DurabilityInfoConfigManager.current();
        assertEquals(reads, DurabilityInfoConfigManager.diskReadCountForTests());
    }

    @Test void existingSchemaTwoFileRemainsByteForByteUnchanged() throws Exception {
        Path config = temporary.resolve("durabilityinfo.json");
        String original = "{\"schemaVersion\":2,\"tooltip\":{\"style\":\"DETAILED\"},"
                + "\"hud\":{\"visibility\":\"DAMAGED_ONLY\",\"scale\":1.0}}\n";
        Files.writeString(config, original);
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        assertEquals(TooltipStyle.DETAILED, DurabilityInfoConfigManager.current().tooltip.style);
        assertEquals(HudVisibilityMode.DAMAGED_ONLY, DurabilityInfoConfigManager.current().hud.visibility);
        assertEquals(1.0, DurabilityInfoConfigManager.current().hud.scale);
        assertEquals(original, Files.readString(config));
    }

    @Test void migratesEverySchemaOneField() throws Exception {
        Files.writeString(temporary.resolve("durabilityinfo.json"), """
                {"showDurabilityNumbers":false,"showPercentage":true,"showBar":false,
                 "showOnUnbreakable":true,"warningThreshold":17,"hudAnchor":"TOP_LEFT",
                 "hudOffsetX":31,"hudOffsetY":42,"showDamageDealt":true,"hudDisplayMode":"PERCENTAGE"}
                """);
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        var c = DurabilityInfoConfigManager.current();
        assertEquals(2, c.schemaVersion);
        assertEquals(TooltipStyle.CUSTOM, c.tooltip.style);
        assertFalse(c.tooltip.showNumbers);
        assertTrue(c.tooltip.showPercentage);
        assertFalse(c.tooltip.showBar);
        assertTrue(c.tooltip.showUnbreakable);
        assertTrue(c.tooltip.showDamageTaken);
        assertEquals(17, c.hud.threshold);
        assertEquals(DurabilityInfoConfig.HudAnchor.TOP_LEFT, c.hud.anchor);
        assertEquals(31, c.hud.offsetX);
        assertEquals(42, c.hud.offsetY);
        assertEquals(DurabilityInfoConfig.HudDisplayMode.PERCENTAGE, c.hud.displayMode);
        assertEquals(HudVisibilityMode.DAMAGED_ONLY, c.hud.visibility);
        assertEquals(1.0, c.hud.scale);
        String migrated = Files.readString(temporary.resolve("durabilityinfo.json"));
        assertTrue(migrated.contains("\"schemaVersion\": 2"));
        assertFalse(migrated.contains("showDurabilityNumbers"));
    }

    @Test void existingPartialSchemaTwoUsesLegacyFallbacksWithoutRewriting() throws Exception {
        Path config = temporary.resolve("durabilityinfo.json");
        String original = "{\"schemaVersion\":2,\"hud\":{\"threshold\":42}}\n";
        Files.writeString(config, original);

        DurabilityInfoConfigManager.initialize(temporary, message -> {});

        assertEquals(HudVisibilityMode.DAMAGED_ONLY, DurabilityInfoConfigManager.current().hud.visibility);
        assertEquals(1.0, DurabilityInfoConfigManager.current().hud.scale);
        assertEquals(42, DurabilityInfoConfigManager.current().hud.threshold);
        assertEquals(original, Files.readString(config));
    }

    @Test void fillsMissingIgnoresUnknownAndClampsInvalidValues() throws Exception {
        Files.writeString(temporary.resolve("durabilityinfo.json"), """
                {"schemaVersion":2,"unknownFutureField":true,
                 "tooltip":{"barWidth":999},"hud":{"scale":99,"threshold":-8},
                 "colors":{"criticalBelow":90,"lowBelow":2,"wornBelow":1}}
                """);
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        var c = DurabilityInfoConfigManager.current();
        assertEquals(60, c.tooltip.barWidth);
        assertEquals(2.0, c.hud.scale);
        assertEquals(0, c.hud.threshold);
        assertTrue(c.colors.criticalBelow < c.colors.lowBelow);
        assertTrue(c.colors.lowBelow < c.colors.wornBelow);
        assertNotNull(c.alerts.armor);
    }

    @Test void corruptJsonIsBackedUpAndDefaultsRestored() throws Exception {
        Path config = temporary.resolve("durabilityinfo.json");
        String corrupt = "{ broken";
        Files.writeString(config, corrupt);
        ArrayList<String> messages = new ArrayList<>();
        DurabilityInfoConfigManager.initialize(temporary, messages::add);
        assertTrue(Files.exists(config));
        assertTrue(Files.readString(config).contains("\"schemaVersion\": 2"));
        Path backup;
        try (var files = Files.list(temporary)) {
            backup = files.filter(p -> p.getFileName().toString().endsWith(".corrupt"))
                    .findFirst().orElseThrow();
        }
        assertEquals(corrupt, Files.readString(backup));
        assertEquals(DurabilityPreset.VANILLA_PLUS, DurabilityInfoConfigManager.current().preset);
        assertEquals(HudVisibilityMode.ALWAYS, DurabilityInfoConfigManager.current().hud.visibility);
        assertEquals(0.85, DurabilityInfoConfigManager.current().hud.scale);
        assertFalse(messages.isEmpty());
    }

    @Test void explicitSaveRemainsPrettyPrintedAndAtomic() throws Exception {
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        DurabilityInfoConfig draft = DurabilityInfoConfigManager.copyCurrent();
        draft.tooltip.style = TooltipStyle.DETAILED;
        assertTrue(DurabilityInfoConfigManager.applyAndSave(draft));
        String json = Files.readString(temporary.resolve("durabilityinfo.json"));
        assertTrue(json.contains("\n  \"schemaVersion\""));
        assertFalse(Files.exists(temporary.resolve("durabilityinfo.json.tmp")));
    }

    @Test void secondInitializationDoesNotRewriteUnchangedFile() throws Exception {
        Path config = temporary.resolve("durabilityinfo.json");
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        String original = Files.readString(config);
        DurabilityInfoConfigManager.initialize(temporary, message -> fail("initializer must be idempotent"));
        assertEquals(0, DurabilityInfoConfigManager.diskReadCountForTests());

        DurabilityInfoConfigManager.resetForTests();
        FileTime marker = FileTime.from(Instant.parse("2020-01-02T03:04:05Z"));
        Files.setLastModifiedTime(config, marker);
        DurabilityInfoConfigManager.initialize(temporary, message -> {});

        assertEquals(original, Files.readString(config));
        assertEquals(marker, Files.getLastModifiedTime(config));
        assertEquals(1, DurabilityInfoConfigManager.diskReadCountForTests());
    }

    @Test void failedInitialCreationKeepsDefaultsAndLogsOnlyOnce() throws Exception {
        Path notADirectory = temporary.resolve("blocked");
        Files.writeString(notADirectory, "regular file");
        ArrayList<String> messages = new ArrayList<>();

        DurabilityInfoConfigManager.initialize(notADirectory, messages::add);
        assertEquals(DurabilityPreset.VANILLA_PLUS, DurabilityInfoConfigManager.current().preset);
        assertEquals(1, messages.stream().filter(message -> message.startsWith("Failed to save")).count());

        DurabilityInfoConfigManager.initialize(notADirectory, messages::add);
        assertEquals(1, messages.stream().filter(message -> message.startsWith("Failed to save")).count());
    }

    @Test void invalidEnumAndNonFiniteNumbersFallBackSafely() throws Exception {
        Files.writeString(temporary.resolve("durabilityinfo.json"), """
                {"schemaVersion":2,"tooltip":{"style":"NOT_A_STYLE"},
                 "hud":{"scale":"NaN"},"notifications":{"scale":"Infinity"}}
                """);
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        var config = DurabilityInfoConfigManager.current();
        assertNotNull(config.tooltip.style);
        assertEquals(0.85, config.hud.scale);
        assertTrue(Double.isFinite(config.notifications.scale));
    }

    @Test void hudEditorSessionAppliesOnlyHudAndCancelUsesLatestApplyBaseline() {
        DurabilityInfoConfigManager.initialize(temporary, message -> {});
        DurabilityInfoConfig parentDraft = DurabilityInfoConfigManager.copyCurrent();
        parentDraft.tooltip.style = TooltipStyle.DETAILED;
        parentDraft.hud.scale = 1.5;
        HudEditorSession session = new HudEditorSession(parentDraft);
        session.markChanged();

        assertTrue(session.applyAndSave());
        assertEquals(1.5, DurabilityInfoConfigManager.current().hud.scale);
        assertEquals(TooltipStyle.VANILLA_PLUS, DurabilityInfoConfigManager.current().tooltip.style);
        assertEquals(DurabilityPreset.CUSTOM, DurabilityInfoConfigManager.current().preset);

        parentDraft.hud.scale = 2.0;
        session.markChanged();
        session.cancel();
        assertEquals(1.5, parentDraft.hud.scale);
        assertEquals(DurabilityPreset.CUSTOM, parentDraft.preset);
    }
}
