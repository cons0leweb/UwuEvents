package uwu.events;

import uwu.events.annotation.AutoSubscriber;
import uwu.events.bus.EventBus;
import uwu.events.bus.Priority;

import java.util.function.Consumer;

/**
 * Фасад для простого доступа к EventBus.
 */
public final class Events {
    private static final EventBus BUS = EventBus.getInstance();

    private Events() {}

    // Быстрые методы подписки
    public static <T> void on(Class<T> eventType, Runnable handler) {
        BUS.subscribe(eventType, e -> handler.run());
    }

    public static <T> void on(Class<T> eventType, Consumer<T> handler) {
        BUS.subscribe(eventType, handler);
    }

    public static <T> void on(Class<T> eventType, Consumer<T> handler, int priority) {
        BUS.subscribe(eventType, handler, priority);
    }

    // Автоподписка
    public static void subscribe(Object object) {
        AutoSubscriber.subscribe(object);
    }

    public static void unsubscribe(Object object) {
        AutoSubscriber.unsubscribe(object);
    }

    // Отправка событий
    public static <T> T post(T event) {
        return BUS.post(event);
    }

    // Получение EventBus для продвинутого использования
    public static EventBus bus() {
        return BUS;
    }
}