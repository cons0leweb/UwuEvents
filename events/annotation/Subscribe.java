package uwu.events.annotation;

import uwu.events.bus.Priority;

import java.lang.annotation.*;

/**
 * Аннотация для автоматической подписки на события.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    int priority() default Priority.NORMAL;
}