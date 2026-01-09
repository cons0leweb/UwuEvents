package uwu.events.events;

import uwu.events.core.Stoppable;

/**
 * Базовая реализация останавливаемого события.
 */
public abstract class AbstractStoppableEvent extends AbstractEvent implements Stoppable {
    private boolean stopped = false;

    @Override
    public void stop() {
        this.stopped = true;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }
}