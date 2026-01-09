package uwu.events.pipeline;

import uwu.events.core.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Паттерн Pipeline для обработки событий.
 */
public class EventPipeline<T extends Event> {

    /** Пример использования:
    *<pre>EventPipeline.of(event)
     *  .filter(e -> !e.isCancelled())
     *  .peek(e -> System.out.println("Processing: " + e))
     *  .map(e -> { e.setSomeField(value); return e; })
     *  .execute();
     * </pre>
     */
    private final List<PipelineStage<T>> stages = new ArrayList<>();
    private final T event;

    private EventPipeline(T event) {
        this.event = event;
    }

    public static <T extends Event> EventPipeline<T> of(T event) {
        return new EventPipeline<>(event);
    }

    public EventPipeline<T> filter(Predicate<T> predicate) {
        stages.add(new FilterStage<>(predicate));
        return this;
    }

    public EventPipeline<T> map(Function<T, T> mapper) {
        stages.add(new MapStage<>(mapper));
        return this;
    }

    public EventPipeline<T> peek(Consumer<T> consumer) {
        stages.add(new PeekStage<>(consumer));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R extends Event> EventPipeline<R> transform(Function<T, R> transformer) {
        stages.add(new TransformStage<>(transformer));
        return (EventPipeline<R>) this;
    }

    public T execute() {
        T current = event;
        for (PipelineStage<T> stage : stages) {
            current = stage.process(current);
            if (current == null) {
                break;
            }
        }
        return current;
    }

    // Стадии пайплайна
    private interface PipelineStage<T> {
        T process(T event);
    }

    private static class FilterStage<T> implements PipelineStage<T> {
        private final Predicate<T> predicate;

        FilterStage(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public T process(T event) {
            return predicate.test(event) ? event : null;
        }
    }

    private static class MapStage<T> implements PipelineStage<T> {
        private final Function<T, T> mapper;

        MapStage(Function<T, T> mapper) {
            this.mapper = mapper;
        }

        @Override
        public T process(T event) {
            return mapper.apply(event);
        }
    }

    private static class PeekStage<T> implements PipelineStage<T> {
        private final Consumer<T> consumer;

        PeekStage(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public T process(T event) {
            consumer.accept(event);
            return event;
        }
    }

    private static class TransformStage<T, R> implements PipelineStage<T> {
        private final Function<T, R> transformer;

        @SuppressWarnings("unchecked")
        TransformStage(Function<T, R> transformer) {
            this.transformer = transformer;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T process(T event) {
            return (T) transformer.apply(event);
        }
    }
}
