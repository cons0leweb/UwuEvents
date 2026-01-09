package uwu.events.bus;

import uwu.events.core.Cancellable;
import uwu.events.core.Stoppable;
import uwu.events.profiler.EventProfiler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Современный EventBus с поддержкой приоритетов, отмены и остановки.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final Map<Object, List<EventListener<?>>> ownerListeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<EventListener<?>>> listenerCache = new ConcurrentHashMap<>();
    private final EventProfiler profiler = EventProfiler.getInstance();

    private boolean dirtyCache = false;

    /**
     * Получить глобальный экземпляр EventBus.
     */
    public static EventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Создать новый экземпляр EventBus (для изоляции).
     */
    public static EventBus create() {
        return new EventBus();
    }

    /**
     * Подписаться на событие.
     */
    public <T> EventListener<T> subscribe(Class<T> eventType, Consumer<T> handler) {
        return subscribe(eventType, handler, Priority.NORMAL, null);
    }

    /**
     * Подписаться на событие с приоритетом.
     */
    public <T> EventListener<T> subscribe(Class<T> eventType, Consumer<T> handler, int priority) {
        return subscribe(eventType, handler, priority, null);
    }

    /**
     * Подписаться на событие с приоритетом и владельцем.
     */
    public <T> EventListener<T> subscribe(Class<T> eventType, Consumer<T> handler, int priority, Object owner) {
        EventListener<T> listener = new EventListener<>(eventType, handler, priority, owner);

        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        dirtyCache = true;

        if (owner != null) {
            ownerListeners.computeIfAbsent(owner, k -> new ArrayList<>()).add(listener);
        }

        return listener;
    }

    /*
    * ВНИМАНИЕ НЕ СОВЕТУЮ ИСПОЛЬЗОВАТЬ RAW МЕТОДЫ ГДЕ-ЛИБО!
    * */

    /**
     * Raw подписка (без типовой безопасности, для рефлексии).
     * ВНИМАНИЕ: Только для внутреннего использования!
     */
    @SuppressWarnings("unchecked")
    public EventListener<Object> subscribeRaw(Class<?> eventType, Consumer<Object> handler, int priority, Object owner) {
        return (EventListener<Object>) subscribe((Class<Object>) eventType, handler, priority, owner);
    }

    /**
     * Raw подписка без владельца.
     */
    @SuppressWarnings("unchecked")
    public EventListener<Object> subscribeRaw(Class<?> eventType, Consumer<Object> handler, int priority) {
        return (EventListener<Object>) subscribe((Class<Object>) eventType, handler, priority, null);
    }

    /**
     * Raw подписка с дефолтным приоритетом.
     */
    @SuppressWarnings("unchecked")
    public EventListener<Object> subscribeRaw(Class<?> eventType, Consumer<Object> handler) {
        return (EventListener<Object>) subscribe((Class<Object>) eventType, handler, Priority.NORMAL, null);
    }


    /**
     * Отписать владельца от всех событий.
     */
    public void unsubscribe(Object owner) {
        List<EventListener<?>> owned = ownerListeners.remove(owner);
        if (owned != null) {
            for (EventListener<?> listener : owned) {
                List<EventListener<?>> list = listeners.get(listener.getEventType());
                if (list != null) {
                    list.remove(listener);
                }
            }
            dirtyCache = true;
        }
    }

    /**
     * Отписать конкретный listener.
     */
    public void unsubscribe(EventListener<?> listener) {
        List<EventListener<?>> list = listeners.get(listener.getEventType());
        if (list != null) {
            list.remove(listener);
            dirtyCache = true;
        }

        if (listener.getOwner() != null) {
            List<EventListener<?>> owned = ownerListeners.get(listener.getOwner());
            if (owned != null) {
                owned.remove(listener);
            }
        }
    }

    /**
     * Отправить событие.
     */
    @SuppressWarnings("unchecked")
    public <T> T post(T event) {
        if (event == null) return null;

        // Получаем слушатели из кеша или сортируем
        List<EventListener<?>> eventListeners = getSortedListeners(event.getClass());

        // Для отменяемых событий
        boolean cancelled = false;
        if (event instanceof Cancellable) {
            cancelled = ((Cancellable) event).isCancelled();
        }

        // Вызываем слушатели
        for (EventListener<?> listener : eventListeners) {
            if (cancelled) break; // Отменённые события не обрабатываются

            ((EventListener<T>) listener).handle(event);

            // Проверяем остановку
            if (event instanceof Stoppable && ((Stoppable) event).isStopped()) {
                break;
            }
        }

        return event;
    }

    /**
     * Получить отсортированные слушатели для типа события.
     */
    private List<EventListener<?>> getSortedListeners(Class<?> eventType) {
        if (dirtyCache) {
            listenerCache.clear();
            dirtyCache = false;
        }

        return listenerCache.computeIfAbsent(eventType, k -> {
            List<EventListener<?>> list = listeners.get(eventType);
            if (list == null) return Collections.emptyList();

            // Сортировка по приоритету (высший приоритет = первым)
            List<EventListener<?>> sorted = new ArrayList<>(list);
            sorted.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
            return Collections.unmodifiableList(sorted);
        });
    }

    /**
     * Очистить всех слушателей.
     */
    public void clear() {
        listeners.clear();
        ownerListeners.clear();
        listenerCache.clear();
        dirtyCache = false;
    }

    /**
     * Проверить, есть ли слушатели для типа события.
     */
    public boolean hasListeners(Class<?> eventType) {
        List<EventListener<?>> list = listeners.get(eventType);
        return list != null && !list.isEmpty();
    }
}