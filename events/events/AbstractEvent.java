package uwu.events.events;

import uwu.events.core.Event;

/**
 * Базовая реализация события.
 */
public abstract class AbstractEvent implements Event {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}