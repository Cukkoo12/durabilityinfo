package com.cukkoo.durabilityinfo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AlertStateTracker {
    private final Map<String, SlotState> states = new HashMap<>(8);

    public Optional<AlertEvent> update(DurabilitySnapshot snapshot, AlertThresholdSet thresholds) {
        if (!DurabilityCalculator.isUsable(snapshot)) {
            states.remove(snapshot == null ? "" : snapshot.slotKey());
            return Optional.empty();
        }
        int percentage = DurabilityCalculator.percentage(snapshot);
        SlotState old = states.get(snapshot.slotKey());
        if (old == null || !old.itemKey.equals(snapshot.itemKey())) {
            states.put(snapshot.slotKey(), new SlotState(snapshot.itemKey(), percentage));
            return Optional.empty();
        }
        AlertThresholdSet.Level crossed = crossing(old.percentage, percentage,
                thresholds.warningEnabled, thresholds.warning, "warning", null);
        crossed = crossing(old.percentage, percentage, thresholds.lowEnabled, thresholds.low, "low", crossed);
        crossed = crossing(old.percentage, percentage, thresholds.criticalEnabled, thresholds.critical, "critical", crossed);
        crossed = crossing(old.percentage, percentage, thresholds.lastChanceEnabled, thresholds.lastChance, "last_chance", crossed);
        old.percentage = percentage;
        return crossed == null ? Optional.empty() : Optional.of(new AlertEvent(snapshot, crossed));
    }

    public void clear() {
        states.clear();
    }

    public int trackedSlotCount() {
        return states.size();
    }

    private static AlertThresholdSet.Level crossing(int before, int after, boolean enabled, int threshold,
                                                     String key, AlertThresholdSet.Level previous) {
        return enabled && before > threshold && after <= threshold
                ? new AlertThresholdSet.Level(key, threshold, true) : previous;
    }

    private static final class SlotState {
        private final String itemKey;
        private int percentage;

        private SlotState(String itemKey, int percentage) {
            this.itemKey = itemKey;
            this.percentage = percentage;
        }
    }

    public record AlertEvent(DurabilitySnapshot snapshot, AlertThresholdSet.Level level) {}
}
