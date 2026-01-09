package uwu.events.core;

/**
 * Событие, которое можно остановить (прервать цепочку обработчиков).
 */
public interface Stoppable extends Event {
    void stop();
    boolean isStopped();
}