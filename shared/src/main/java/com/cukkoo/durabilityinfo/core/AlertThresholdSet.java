package com.cukkoo.durabilityinfo.core;

import java.util.List;

public final class AlertThresholdSet {
    public boolean warningEnabled = true;
    public int warning = 25;
    public boolean lowEnabled = true;
    public int low = 10;
    public boolean criticalEnabled = true;
    public int critical = 5;
    public boolean lastChanceEnabled = true;
    public int lastChance = 1;

    public List<Level> descending() {
        return List.of(
                new Level("warning", warning, warningEnabled),
                new Level("low", low, lowEnabled),
                new Level("critical", critical, criticalEnabled),
                new Level("last_chance", lastChance, lastChanceEnabled));
    }

    public record Level(String key, int percentage, boolean enabled) {}
}
