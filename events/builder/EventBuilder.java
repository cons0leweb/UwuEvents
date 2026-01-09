package uwu.events.builder;

import uwu.events.core.Cancellable;
import uwu.events.core.Stoppable;
import uwu.events.events.AbstractEvent;
import uwu.events.events.AbstractStoppableEvent;

/**
 * Fluent builder для создания событий.
 */
public class EventBuilder {

    /**
     * Пример использования:
     *
     * <pre>
     * AbstractEvent event = EventBuilder.create()
     *         .cancellable()
     *         .withData(someObject)
     *         .build();
     * </pre>
     */


    public static EventBuilder create() {
        return new EventBuilder();
    }

    private boolean cancellable = false;
    private boolean stoppable = false;
    private Object data = null;

    public EventBuilder cancellable() {
        this.cancellable = true;
        return this;
    }

    public EventBuilder stoppable() {
        this.stoppable = true;
        return this;
    }

    public EventBuilder withData(Object data) {
        this.data = data;
        return this;
    }

    public AbstractEvent build() {
        if (stoppable) {
            return new AbstractStoppableEvent() {
                private final Object eventData = data;

                public Object getData() {
                    return eventData;
                }
            };
        } else if (cancellable) {
            return new AbstractEvent() {
                private final Object eventData = data;

                public Object getData() {
                    return eventData;
                }
            };
        } else {
            return new AbstractEvent() {};
        }
    }


}