package com.cukkoo.durabilityinfo.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DurabilityChangeTracker {
    private static final int MAX_VISIBLE = 3;
    private final Map<String, Previous> previous = new HashMap<>(16);
    private final ArrayDeque<ChangeNotification> queue = new ArrayDeque<>(MAX_VISIBLE);

    public void update(DurabilitySnapshot snapshot, long nowMillis) {
        if (!DurabilityCalculator.isUsable(snapshot)) {
            if (snapshot != null) previous.remove(snapshot.slotKey());
            return;
        }
        int remaining = DurabilityCalculator.remaining(snapshot);
        Previous old = previous.put(snapshot.slotKey(), new Previous(snapshot.itemKey(), remaining));
        if (old == null || !old.itemKey.equals(snapshot.itemKey()) || old.remaining == remaining) return;
        int delta = remaining - old.remaining;
        ChangeNotification last = queue.peekLast();
        if (last != null && last.itemKey.equals(snapshot.itemKey()) && last.delta == delta
                && nowMillis - last.createdAtMillis <= 500L) {
            queue.removeLast();
            queue.addLast(new ChangeNotification(snapshot.itemKey(), snapshot.displayName(),
                    delta, remaining, snapshot.maxDurability(), nowMillis, last.count + 1));
        } else {
            while (queue.size() >= MAX_VISIBLE) queue.removeFirst();
            queue.addLast(new ChangeNotification(snapshot.itemKey(), snapshot.displayName(),
                    delta, remaining, snapshot.maxDurability(), nowMillis, 1));
        }
    }

    public List<ChangeNotification> visible(long nowMillis, long durationMillis) {
        trim(nowMillis, durationMillis);
        return List.copyOf(queue);
    }

    public Iterable<ChangeNotification> visibleIterable(long nowMillis, long durationMillis) {
        trim(nowMillis, durationMillis);
        return queue;
    }

    public int visibleCount(long nowMillis, long durationMillis) {
        trim(nowMillis, durationMillis);
        return queue.size();
    }

    private void trim(long nowMillis, long durationMillis) {
        while (!queue.isEmpty() && nowMillis - queue.peekFirst().createdAtMillis > durationMillis) queue.removeFirst();
    }

    public void clear() {
        previous.clear();
        queue.clear();
    }

    public record ChangeNotification(String itemKey, String displayName, int delta, int remaining,
                                     int maximum, long createdAtMillis, int count) {}

    private record Previous(String itemKey, int remaining) {}
}
