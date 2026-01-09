package uwu.events.scheduler;

import uwu.events.bus.EventBus;
import uwu.events.core.Event;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Планировщик отложенных событий.
 */
public class EventScheduler {
    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "EventScheduler");
                t.setDaemon(true);
                return t;
            });

    private final EventBus eventBus;

    public EventScheduler() {
        this(EventBus.getInstance());
    }

    public EventScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public ScheduledFuture<?> schedule(Supplier<Event> eventSupplier, long delay, TimeUnit unit) {
        return EXECUTOR.schedule(() -> {
            Event event = eventSupplier.get();
            if (event != null) {
                eventBus.post(event);
            }
        }, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Supplier<Event> eventSupplier,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return EXECUTOR.scheduleAtFixedRate(() -> {
            Event event = eventSupplier.get();
            if (event != null) {
                eventBus.post(event);
            }
        }, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Supplier<Event> eventSupplier,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        return EXECUTOR.scheduleWithFixedDelay(() -> {
            Event event = eventSupplier.get();
            if (event != null) {
                eventBus.post(event);
            }
        }, initialDelay, delay, unit);
    }

    public void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}