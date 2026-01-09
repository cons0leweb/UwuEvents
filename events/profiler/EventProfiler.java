package uwu.events.profiler;

import uwu.events.bus.EventBus;
import uwu.events.bus.EventListener;
import uwu.events.core.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Профайлер для измерения производительности событий.
 */
public class EventProfiler {
    private static final EventProfiler INSTANCE = new EventProfiler();

    private final Map<Class<?>, EventStats> stats = new ConcurrentHashMap<>();
    private final Map<EventListener<?>, ListenerStats> listenerStats = new ConcurrentHashMap<>();
    private boolean enabled = false;

    public static EventProfiler getInstance() {
        return INSTANCE;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public <T extends Event> Consumer<T> wrapListener(EventListener<T> listener, Consumer<T> handler) {
        if (!enabled) return handler;

        return event -> {
            long startTime = System.nanoTime();
            try {
                handler.accept(event);
            } finally {
                long endTime = System.nanoTime();
                long duration = endTime - startTime;

                // Обновляем статистику
                updateStats(event.getClass(), duration);
                updateListenerStats(listener, duration);
            }
        };
    }

    private void updateStats(Class<?> eventType, long duration) {
        EventStats eventStats = stats.computeIfAbsent(eventType, k -> new EventStats());
        eventStats.record(duration);
    }

    private void updateListenerStats(EventListener<?> listener, long duration) {
        ListenerStats listenerStats = this.listenerStats.computeIfAbsent(listener, k -> new ListenerStats());
        listenerStats.record(duration);
    }

    public Map<Class<?>, EventStats> getEventStats() {
        return Collections.unmodifiableMap(stats);
    }

    public Map<EventListener<?>, ListenerStats> getListenerStats() {
        return Collections.unmodifiableMap(listenerStats);
    }

    public void reset() {
        stats.clear();
        listenerStats.clear();
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("@@@ Event Profiler Report @@@\n");

        sb.append("\nEvent Statistics:\n");
        stats.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().getTotalTime(), a.getValue().getTotalTime()))
                .forEach(entry -> {
                    EventStats stat = entry.getValue();
                    sb.append(String.format("  %s: calls=%d, avg=%.2fms, total=%.2fms\n",
                            entry.getKey().getSimpleName(),
                            stat.getCallCount(),
                            stat.getAverageTime() / 1_000_000.0,
                            stat.getTotalTime() / 1_000_000.0));
                });

        sb.append("\nSlowest Listeners:\n");
        listenerStats.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().getMaxTime(), a.getValue().getMaxTime()))
                .limit(10)
                .forEach(entry -> {
                    ListenerStats stat = entry.getValue();
                    sb.append(String.format("  %s: avg=%.2fms, max=%.2fms\n",
                            entry.getKey().getClass().getSimpleName(),
                            stat.getAverageTime() / 1_000_000.0,
                            stat.getMaxTime() / 1_000_000.0));
                });

        return sb.toString();
    }

    public static class EventStats {
        private final AtomicLong callCount = new AtomicLong();
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong maxTime = new AtomicLong();

        public void record(long duration) {
            callCount.incrementAndGet();
            totalTime.addAndGet(duration);
            maxTime.updateAndGet(current -> Math.max(current, duration));
        }

        public long getCallCount() { return callCount.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getAverageTime() {
            long count = callCount.get();
            return count > 0 ? totalTime.get() / count : 0;
        }
        public long getMaxTime() { return maxTime.get(); }
    }

    public static class ListenerStats {
        private final AtomicLong callCount = new AtomicLong();
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong maxTime = new AtomicLong();

        public void record(long duration) {
            callCount.incrementAndGet();
            totalTime.addAndGet(duration);
            maxTime.updateAndGet(current -> Math.max(current, duration));
        }

        public long getCallCount() { return callCount.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getAverageTime() {
            long count = callCount.get();
            return count > 0 ? totalTime.get() / count : 0;
        }
        public long getMaxTime() { return maxTime.get(); }
    }
}