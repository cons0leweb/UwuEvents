package uwu.events.bus;

import java.util.function.Consumer;

/**
 * Обработчик события с приоритетом.
 */
public class EventListener<T> {
    private final Class<T> eventType;
    private final Consumer<T> handler;
    private final int priority;
    private final Object owner;
    private boolean active = true;

    public EventListener(Class<T> eventType, Consumer<T> handler, int priority, Object owner) {
        this.eventType = eventType;
        this.handler = handler;
        this.priority = priority;
        this.owner = owner;
    }

    public void handle(T event) {
        if (active) {
            handler.accept(event);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Getters
    public Class<T> getEventType() { return eventType; }
    public Consumer<T> getHandler() { return handler; }
    public int getPriority() { return priority; }
    public Object getOwner() { return owner; }
    public boolean isActive() { return active; }
}