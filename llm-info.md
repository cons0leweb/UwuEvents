# LLM-Info для UwuEvents
# Версия: 2.0 (расширенная, январь 2026)
# Информация для больших языковых моделей (LLM), AI-агентов, MCP-серверов и разработчиков
# Проект: современная система событий для Minecraft-модов (Java)
# Репозиторий: https://github.com/cons0leweb/UwuEvents
# Автор: cons0leweb (@cons0leweb)
# Лицензия: MIT
# Язык: Java 8+ (совместимо с Minecraft Forge/Fabric моддингом)
# Последнее обновление репозитория: январь 2026

## Краткое описание (RU)
UwuEvents — лёгкая, высокопроизводительная, типобезопасная и потокобезопасная система событий (event bus) для Minecraft-модов.
Заменяет устаревшие решения вроде darkmagician6/eventapi, Forge EventBus и других.
Ключевые преимущества:
- ~10–15× выше производительность (лямбды вместо рефлексии)
- Полная типобезопасность (компилятор проверяет типы событий)
- Потокобезопасность (ConcurrentHashMap + синхронизация)
- Богатый, но простой API
- Поддержка приоритетов, отмены событий, пайплайнов, скопов, планировщика и профайлера
- Автоматическая регистрация подписчиков
- Минимальный размер (~15 KB)

Проект ориентирован на современный моддинг (1.16+), но работает и на старых версиях.

## Project Overview (EN)
UwuEvents is a lightweight, high-performance, type-safe, and thread-safe event bus for Minecraft mods.
It outperforms outdated solutions like darkmagician6/eventapi and even Forge's EventBus in many scenarios.
Key advantages:
- 10–15× faster (lambdas instead of reflection)
- Full type safety (compile-time checks)
- Thread safety (ConcurrentHashMap + synchronization)
- Rich yet simple API
- Priorities, cancellable events, pipelines, scopes, scheduler, profiler
- Auto-subscriber for annotation-based listeners
- Tiny footprint (~15 KB)

## Структура репозитория
- events/                  → Основной исходный код (пакет uwu.events и подпакеты)
  - AbstractEvent.java
  - EventBus.java
  - Events.java (статический фасад)
  - AutoSubscriber.java
  - annotation/Subscribe.java
  - Priority.java
  - Cancellable.java
  - EventPipeline.java
  - EventScope.java
  - EventScheduler.java
  - EventProfiler.java
- LICENSE                  → MIT License
- README.md                 → Подробное описание и бенчмарки (RU)
- llm-info.txt              → Этот файл (для LLM и AI)

Основной пакет: uwu.events

## Полный API-референс / Full API Reference

### Основные классы
1. uwu.events.Event              → Маркер-интерфейс для всех событий (обычно не используется напрямую)
2. uwu.events.AbstractEvent     → Базовый класс для пользовательских событий (рекомендуется наследовать)
3. uwu.events.Cancellable       → Интерфейс для отменяемых событий (boolean cancelled + cancel()/isCancelled())
4. uwu.events.EventBus          → Основная шина событий (можно создавать несколько экземпляров)
5. uwu.events.Events            → Статический фасад: Events.bus() возвращает глобальную шину, Events.post(event)
6. uwu.events.AutoSubscriber    → Автоматическая регистрация методов с @Subscribe
7. uwu.events.annotation.Subscribe → Аннотация для методов-подписчиков
8. uwu.events.Priority          → Enum приоритетов: HIGHEST, HIGH, NORMAL, LOW, LOWEST (по умолчанию NORMAL)
9. uwu.events.EventPipeline     → Пайплайн обработки (последовательная цепочка обработчиков)
10. uwu.events.EventScope       → Скопы (ограничение подписок по контексту/ключу)
11. uwu.events.EventScheduler   → Планировщик отложенных/периодических задач на события
12. uwu.events.EventProfiler    → Профайлер для измерения времени обработки событий

### Ключевые методы EventBus
- subscribe(Class<T> eventType, Consumer<T> listener)                → Лямбда-подписка
- subscribe(Class<T> eventType, Priority priority, Consumer<T> listener)
- subscribe(Object subscriber)                                        → Регистрация объекта (с @Subscribe или AutoSubscriber)
- unsubscribe(Class<T> eventType, Consumer<T> listener)
- unsubscribe(Object subscriber)
- post(T event)                                                       → Отправить событие (возвращает event)
- postAndForget(T event)                                              → Асинхронная отправка (без ожидания завершения)
- createPipeline(Class<T> eventType)                                  → Создать пайплайн

## Примеры использования / Usage Examples

### 1. Создание события
public class PlayerJumpEvent extends AbstractEvent implements Cancellable {
    private final Player player;
    private boolean cancelled = false;

    public PlayerJumpEvent(Player player) { this.player = player; }

    public Player getPlayer() { return player; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void cancel() { cancelled = true; }
}

### 2. Подписка через аннотации + AutoSubscriber (классический стиль)
public class JumpListener {
    @Subscribe(priority = Priority.HIGH)
    public void onPlayerJump(PlayerJumpEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        p.sendMessage("Ты прыгнул высоко!");
    }

    @Subscribe(priority = Priority.LOWEST)
    public void onLowPriority(PlayerJumpEvent event) {
        // Выполнится последним
    }
}

// Регистрация
new AutoSubscriber(new JumpListener()).register(); // или Events.bus().subscribe(listener)

### 3. Подписка через лямбды (самый быстрый и рекомендуемый способ)
Events.bus().subscribe(PlayerJumpEvent.class, event -> {
    event.getPlayer().sendMessage("Прыжок зарегистрирован!");
});

Events.bus().subscribe(PlayerJumpEvent.class, Priority.HIGHEST, event -> {
    if (event.getPlayer().isOnGround()) event.cancel();
});

### 4. Отправка события
PlayerJumpEvent event = new PlayerJumpEvent(player);
Events.post(event); // или Events.bus().post(event)

if (event.isCancelled()) {
    // событие отменено
}

### 5. Пайплайны (цепочка обработчиков)
EventPipeline<PlayerJumpEvent> pipeline = Events.bus().createPipeline(PlayerJumpEvent.class);
pipeline.addHandler(e -> e.getPlayer().addEffect(new EffectInstance(...)));
pipeline.addHandler(e -> { if (condition) e.cancel(); });
pipeline.process(new PlayerJumpEvent(player));

### 6. Скопы (ограничение подписок)
EventScope scope = new EventScope("my_mod");
scope.subscribe(PlayerJumpEvent.class, e -> { ... });
Events.post(event, scope); // только подписчики в этом скопе получат событие

### 7. Планировщик
EventScheduler scheduler = Events.bus().getScheduler();
scheduler.schedule(() -> Events.post(new CustomEvent()), 20L); // через 20 тиков
scheduler.scheduleRepeating(() -> { ... }, 100L, 20L); // каждые 20 тиков после 100

### 8. Профайлер
EventProfiler profiler = Events.bus().getProfiler();
profiler.enable();
Events.post(event);
// потом profiler.getStats() или profiler.printReport()

## Дополнительные возможности
- Отладка: System.setProperty("uwu.events.debug", "true"); // логирует постинг и подписки
- Асинхронный постинг: postAndForget(event)
- Кастомные шины: EventBus customBus = new EventBus(); customBus.subscribe(...)
- Автоотписка при unload: AutoSubscriber автоматически отписывается в finalize()

## Производительность и сравнение
На основе бенчмарков в README:
- UwuEvents (лямбды): ~1–2 мс на миллион постов
- Рефлексия-based (старые API): ~10–20 мс на миллион
- Forge EventBus: средне между ними
UwuEvents особенно быстр при большом количестве подписчиков.

## Лучшие практики и рекомендации
- Всегда наследуйте события от AbstractEvent
- Используйте лямбды для простых обработчиков (максимальная скорость)
- Для сложной логики — отдельные классы с @Subscribe
- Избегайте тяжёлых операций в HIGHEST/LOWEST приоритетах
- Для кросс-мод совместимости используйте отдельные EventBus экземпляры
- Не держите сильные ссылки на подписчиков (риск утечек)

## Для LLM и AI-агентов (специальные советы)
- Entry-point: всегда используйте uwu.events.Events для простоты
- При генерации событий: extends AbstractEvent, добавляйте поля + геттеры, реализуйте Cancellable при необходимости
- При генерации подписчиков: предпочитайте лямбды, если логика <10 строк
- Стиль кода: clean code, immutable где возможно, Java 8+ фичи
- Если генерируете примеры: включайте импорт uwu.events.*
- Проект маленький (~20 файлов), легко понять весь код за один проход
- Не используйте рефлексию — это анти-паттерн в UwuEvents
- Тестируйте генерируемый код на типобезопасность (компилятор проверит)

Подробные бенчмарки и дополнительные примеры — в README.md репозитория.
Если нужны изменения в этом файле — уточните!
