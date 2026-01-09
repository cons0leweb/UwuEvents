package uwu.events.core;

/**
 * Событие, которое можно отменить.
 */
public interface Cancellable extends Event {
    boolean isCancelled();
    void setCancelled(boolean cancelled);

    default void cancel() {
        setCancelled(true);
    }
}