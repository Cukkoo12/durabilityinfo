package com.cukkoo.durabilityinfo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DurabilityCalculatorTest {
    @Test void calculatesRemainingDamageAndPercentageSafely() {
        DurabilitySnapshot snapshot = new DurabilitySnapshot("main", "pick", "Pickaxe", 500, 265, true, false, false);
        assertEquals(235, DurabilityCalculator.remaining(snapshot));
        assertEquals(265, DurabilityCalculator.damageTaken(snapshot));
        assertEquals(47, DurabilityCalculator.percentage(snapshot));
    }

    @Test void clampsBrokenAndInvalidItems() {
        assertEquals(0, DurabilityCalculator.percentage(new DurabilitySnapshot("main", "x", "X", 0, 4, true, false, false)));
        assertEquals(0, DurabilityCalculator.remaining(new DurabilitySnapshot("main", "x", "X", 10, 99, true, false, false)));
        assertEquals(100, DurabilityCalculator.percentage(new DurabilitySnapshot("main", "x", "X", 10, -2, true, false, false)));
        assertFalse(DurabilityCalculator.isUsable(DurabilitySnapshot.empty("main")));
    }

    @Test void colorThresholdsAreConsistent() {
        var colors = new DurabilityInfoConfig.ColorConfig();
        assertEquals(DurabilityColorScale.Band.HEALTHY, DurabilityColorScale.band(51, colors));
        assertEquals(DurabilityColorScale.Band.WORN, DurabilityColorScale.band(50, colors));
        assertEquals(DurabilityColorScale.Band.LOW, DurabilityColorScale.band(25, colors));
        assertEquals(DurabilityColorScale.Band.CRITICAL, DurabilityColorScale.band(5, colors));
    }
}
