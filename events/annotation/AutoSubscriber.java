package uwu.events.annotation;

import uwu.events.bus.EventBus;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Автоматически подписывает методы с аннотацией @Subscribe.
 */
public class AutoSubscriber {
    private static final Map<Object, Map<Method, Object>> registeredMethods = new HashMap<>();

    /**
     * Подписать все методы объекта с @Subscribe.
     */
    public static void subscribe(Object object) {
        Class<?> clazz = object.getClass();
        Map<Method, Object> methods = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1) {
                    Subscribe annotation = method.getAnnotation(Subscribe.class);
                    Class<?> eventType = params[0];

                    // Создаём лямбду с правильным типом через рефлексию
                    Consumer<Object> handler = createHandler(object, method, eventType);

                    // Используем raw type для подписки
                    @SuppressWarnings("unchecked")
                    uwu.events.bus.EventListener<Object> listener =
                            (uwu.events.bus.EventListener<Object>) EventBus.getInstance()
                                    .subscribeRaw(eventType, handler, annotation.priority(), object);

                    methods.put(method, listener);
                }
            }
        }

        if (!methods.isEmpty()) {
            registeredMethods.put(object, methods);
        }
    }

    /**
     * Создать обработчик через reflection proxy.
     */
    private static Consumer<Object> createHandler(Object object, Method method, Class<?> eventType) {
        return event -> {
            try {
                if (eventType.isInstance(event)) {
                    method.setAccessible(true);
                    method.invoke(object, event);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event handler for " +
                        method.getName() + " in " + object.getClass().getName(), e);
            }
        };
    }

    /**
     * Отписать все методы объекта.
     */
    public static void unsubscribe(Object object) {
        EventBus.getInstance().unsubscribe(object);

        Map<Method, Object> methods = registeredMethods.remove(object);
        if (methods != null) {
            // Также отписываем каждый listener индивидуально
            for (Object listener : methods.values()) {
                if (listener instanceof uwu.events.bus.EventListener) {
                    @SuppressWarnings("unchecked")
                    uwu.events.bus.EventListener<Object> eventListener =
                            (uwu.events.bus.EventListener<Object>) listener;
                    EventBus.getInstance().unsubscribe(eventListener);
                }
            }
        }
    }

    /**
     * Очистить все подписки.
     */
    public static void clear() {
        for (Object object : registeredMethods.keySet().toArray(new Object[0])) {
            unsubscribe(object);
        }
    }
}