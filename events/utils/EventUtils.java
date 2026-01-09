package uwu.events.utils;

import uwu.events.Events;
import uwu.events.bus.EventListener;
import uwu.events.core.Event;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * Утилиты для быстрой работы с событиями.
 */
public final class EventUtils {

    private EventUtils() {}

    // Быстрая отправка события
    public static void fire(Event event) {
        Events.post(event);
    }

    // Одноразовая подписка
    public static <T extends Event> void once(Class<T> eventType, Consumer<T> handler) {
        EventListener<T>[] listenerHolder = new EventListener[1];
        listenerHolder[0] = Events.bus().subscribe(eventType, event -> {
            handler.accept(event);
            if (listenerHolder[0] != null) {
                Events.bus().unsubscribe(listenerHolder[0]);
            }
        });
    }

    // Подписка с условием
    public static <T extends Event> void when(Class<T> eventType,
                                              java.util.function.Predicate<T> condition,
                                              Consumer<T> handler) {
        Events.bus().subscribe(eventType, event -> {
            if (condition.test(event)) {
                handler.accept(event);
            }
        });
    }

    // Debounce события (игнорирует частые вызовы)
    public static <T extends Event> void debounce(Class<T> eventType,
                                                  Consumer<T> handler,
                                                  long delayMillis) {
        final java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>>
                futureRef = new java.util.concurrent.atomic.AtomicReference<>();

        Events.bus().subscribe(eventType, event -> {
            ScheduledFuture<?> future = futureRef.get();
            if (future != null) {
                future.cancel(false);
            }

            ScheduledFuture<?> newFuture = java.util.concurrent.Executors
                    .newSingleThreadScheduledExecutor()
                    .schedule(() -> handler.accept(event), delayMillis,
                            java.util.concurrent.TimeUnit.MILLISECONDS);

            futureRef.set(newFuture);
        });
    }

    // Троттлинг события (ограничение частоты вызова)
    public static <T extends Event> void throttle(Class<T> eventType,
                                                  Consumer<T> handler,
                                                  long periodMillis) {
        final java.util.concurrent.atomic.AtomicBoolean
                waiting = new java.util.concurrent.atomic.AtomicBoolean(false);

        Events.bus().subscribe(eventType, event -> {
            if (!waiting.getAndSet(true)) {
                handler.accept(event);
                java.util.concurrent.Executors
                        .newSingleThreadScheduledExecutor()
                        .schedule(() -> waiting.set(false), periodMillis,
                                java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        });
    }
}