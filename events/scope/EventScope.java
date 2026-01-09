package uwu.events.scope;

import uwu.events.bus.EventBus;
import uwu.events.bus.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Область видимости для временных подписок.
 */
public class EventScope implements AutoCloseable {

    /**
     * Пример использования с try-with-resources:
     * <pre>
     *     try (EventScope scope = new EventScope()) {
     *          scope.subscribe(RenderEvent.class, e -> { ... });
     *          // Подписки автоматически удаляются при выходе из блока
     *      }
     *      </pre>
     * */
    private final List<EventListener<?>> listeners = new ArrayList<>();
    private final EventBus eventBus;
    private boolean closed = false;

    public EventScope() {
        this(EventBus.getInstance());
    }

    public EventScope(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public <T> EventListener<T> subscribe(Class<T> eventType, Consumer<T> handler) {
        checkClosed();
        EventListener<T> listener = eventBus.subscribe(eventType, handler);
        listeners.add(listener);
        return listener;
    }

    public <T> EventListener<T> subscribe(Class<T> eventType, Consumer<T> handler, int priority) {
        checkClosed();
        EventListener<T> listener = eventBus.subscribe(eventType, handler, priority);
        listeners.add(listener);
        return listener;
    }

    public void unsubscribe(EventListener<?> listener) {
        checkClosed();
        eventBus.unsubscribe(listener);
        listeners.remove(listener);
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("EventScope is already closed");
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            for (EventListener<?> listener : listeners) {
                eventBus.unsubscribe(listener);
            }
            listeners.clear();
        }
    }


}