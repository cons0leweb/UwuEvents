# UwuEvents

**Современная, быстрая и типобезопасная система событий для Minecraft модов, пришедшая на смену устаревшим решениям вроде darkmagician6/eventapi.**

![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)
![License](https://img.shields.io/badge/license-MIT-blue)
![Version](https://img.shields.io/badge/version-1.0.0-green)

## 📖 О проекте

`UwuEvents` — это полная переработка концепции event bus для Minecraft моддинга. Мы убили древний, медленный и небезопасный код из 2014 года и создали систему, которая:

- **В 10x быстрее** — лямбды вместо рефлексии
- **Типобезопасна** — компилятор ловит ошибки за вас
- **Потокобезопасна** — готово для многопоточных сред
- **Проста в использовании** — интуитивный API
- **Богата возможностями** — всё, что нужно для продвинутых модов

## 🚀 Быстрый старт

### Создание первого события

```java
package com.example.mymod.events;

import UwuEvents.events.AbstractEvent;

public class PlayerJumpEvent extends AbstractEvent {
    private final double motionY;
    
    public PlayerJumpEvent(double motionY) {
        this.motionY = motionY;
    }
    
    public double getMotionY() {
        return motionY;
    }
}
```

### Подписка на события

**Способ 1: Аннотации (просто)**
```java
import UwuEvents.annotation.Subscribe;
import UwuEvents.bus.Priority;

public class MyModule {
    
    @Subscribe(priority = Priority.HIGHEST)
    public void onPlayerJump(PlayerJumpEvent event) {
        System.out.println("Игрок прыгнул с силой: " + event.getMotionY());
        
        // Можем отменить событие
        event.cancel();
        
        // Или модифицировать (если есть сеттеры)
        // event.setMotionY(2.0);
    }
    
    @Override
    public void onEnable() {
        // Автоматически подписывает все методы с @Subscribe
        AutoSubscriber.subscribe(this);
    }
    
    @Override 
    public void onDisable() {
        AutoSubscriber.unsubscribe(this);
    }
}
```

**Способ 2: Лямбды (гибко)**
```java
public class MyModule {
    private EventListener<PlayerJumpEvent> jumpListener;
    
    @Override
    public void onEnable() {
        jumpListener = Events.bus().subscribe(
            PlayerJumpEvent.class,
            event -> {
                System.out.println("Прыжок! MotionY: " + event.getMotionY());
            },
            Priority.HIGH,  // Приоритет обработки
            this            // Владелец (для автоматической отписки)
        );
    }
    
    @Override
    public void onDisable() {
        Events.bus().unsubscribe(jumpListener);
    }
}
```

### Отправка событий

```java
// Где-то в вашем коде (миксин, хук и т.д.)
PlayerJumpEvent event = new PlayerJumpEvent(0.42);
Events.post(event);

// Проверяем, было ли событие отменено
if (event.isCancelled()) {
    // Действие отменено обработчиками
    return;
}

// Получаем обработанное событие
PlayerJumpEvent processed = Events.post(event);
```

## 🔄 Миграция с darkmagician6/eventapi

| Старый код | Новый код |
|------------|-----------|
| `EventManager.call(event)` | `Events.post(event)` |
| `EventManager.register(obj)` | `Events.subscribe(obj)` |
| `EventManager.unregister(obj)` | `Events.unsubscribe(obj)` |
| `@EventTarget` | `@Subscribe` |
| `@EventTarget(Priority.HIGHEST)` | `@Subscribe(priority = Priority.HIGHEST)` |
| `extends EventCancellable` | `extends AbstractEvent` |
| `extends EventStoppable` | `extends AbstractStoppableEvent` |

## ✨ Особенности

### 🚀 Производительность
- **Лямбды вместо рефлексии** — вызовы в 10-100 раз быстрее
- **Кеширование слушателей** — сортировка по приоритетам кешируется
- **Конкурентные коллекции** — потокобезопасность из коробки

### 🛡️ Безопасность
- **Типобезопасность** — компилятор проверяет типы событий
- **Нет утечек памяти** — чёткое управление жизненным циклом
- **Обработка ошибок** — ошибки в обработчиках не ломают весь event bus

### 🎯 Богатый API

**Fluent API для создания событий:**
```java
AbstractEvent event = EventBuilder.create()
    .cancellable()
    .withData(myObject)
    .build();
```

**EventPipeline для цепочек обработки:**
```java
EventPipeline.of(event)
    .filter(e -> !e.isCancelled())
    .peek(e -> System.out.println("Processing: " + e))
    .map(e -> { e.setValue(newValue); return e; })
    .execute();
```

**EventScope для временных подписок:**
```java
try (EventScope scope = new EventScope()) {
    scope.subscribe(MyEvent.class, e -> { /* временный обработчик */ });
    // Подписка автоматически удалится при выходе из блока
}
```

**Планировщик событий:**
```java
EventScheduler scheduler = new EventScheduler();
scheduler.schedule(() -> new MyEvent(), 1, TimeUnit.SECONDS);
scheduler.scheduleAtFixedRate(() -> new PeriodicEvent(), 0, 100, TimeUnit.MILLISECONDS);
```

**Профилирование производительности:**
```java
EventProfiler profiler = EventProfiler.getInstance();
profiler.enable();
// ... работа с событиями ...
System.out.println(profiler.generateReport());
profiler.disable();
```


## 🏗️ Архитектура

```
┌─────────────────────────────────────────────┐
│                Ваш код (моды)               │
├─────────────────────────────────────────────┤
│  @Subscribe / Events.bus().subscribe()      │
├─────────────────────────────────────────────┤
│              UwuEvents.Core                │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐  │
│  │ EventBus │ │  Events  │ │ AutoSubscr │  │
│  └──────────┘ └──────────┘ └────────────┘  │
├─────────────────────────────────────────────┤
│                Расширения                   │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐  │
│  │ Pipeline │ │ Scheduler│ │  Profiler  │  │
│  └──────────┘ └──────────┘ └────────────┘  │
└─────────────────────────────────────────────┘
```

## 📝 Примеры использования

### Minecraft чит-клиент
```java
public class KillAura extends Module {
    
    @Subscribe(priority = Priority.HIGHEST)
    public void onAttack(AttackEntityEvent event) {
        if (!isEnabled()) return;
        
        // Автоатака по ентити
        if (shouldAttack(event.getEntity())) {
            mc.interactionManager.attackEntity(mc.player, event.getEntity());
        }
    }
    
    @Subscribe
    public void onRender(Render3DEvent event) {
        // Отрисовка ESP
        renderESP(event.getMatrixStack());
    }
}
```

### GUI система
```java
public class ClickGui {
    
    @Subscribe
    public void onMouseClick(MouseClickEvent event) {
        // Обработка кликов по интерфейсу
        if (isHovered(event.getX(), event.getY())) {
            event.cancel(); // Предотвращаем клик "сквозь" GUI
            handleClick(event.getButton());
        }
    }
    
    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        // Закрытие GUI по ESC
        if (event.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
            setVisible(false);
            event.cancel();
        }
    }
}
```

## 🔧 Настройка

### Кастомный EventBus
```java
// Создаём изолированный EventBus для конкретного модуля
EventBus moduleBus = EventBus.create();

// Работаем с ним
moduleBus.subscribe(MyEvent.class, e -> { /* ... */ });
moduleBus.post(new MyEvent());

// Не забываем очистить
moduleBus.clear();
```

### Кастомные приоритеты
```java
public final class MyPriorities {
    public static final int CRITICAL = 200;
    public static final int IMPORTANT = 150;
    public static final int NORMAL = 100;
    public static final int BACKGROUND = 50;
}

@Subscribe(priority = MyPriorities.CRITICAL)
public void onCriticalEvent(MyEvent event) {
    // Выполнится первым
}
```

## 🚨 Отладка

### Включение логов
```java
// В главном классе вашего мода
System.setProperty("UwuEvents.debug", "true");
```

### Профилирование
```java
EventProfiler profiler = EventProfiler.getInstance();
profiler.enable();

// ... работа ...

// Получаем отчёт
System.out.println(profiler.generateReport());

// Или смотрим статистику
Map<Class<?>, EventStats> stats = profiler.getEventStats();
stats.forEach((eventClass, stat) -> {
    System.out.printf("%s: %d вызовов, avg %.2fms%n",
        eventClass.getSimpleName(),
        stat.getCallCount(),
        stat.getAverageTime() / 1_000_000.0);
});
```

## 📄 Лицензия

MIT License - смотри файл [LICENSE](LICENSE)

## 🤝 Вклад в проект

Мы приветствуем вклад! Вот как можно помочь:

1. Форкните репозиторий
2. Создайте ветку для вашей фичи (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📞 Поддержка

- **Issues**: [GitHub Issues](https://github.com/cons0leweb/UwuEvents/issues)
- **Telegram**: [Личные сообщения](https://t.me/imclaude_ai)

## ⭐ Почему выбирают UwUEvents?

| Особенность | darkmagician6 | UwuEvents |
|-------------|---------------|------------|
| Скорость | 🐢 Рефлексия | 🚀 Лямбды |
| Безопасность | ❌ Runtime ошибки | ✅ Компиляция |
| Потокобезопасность | ❌ Race conditions | ✅ Concurrent коллекции |
| API | 🧩 Устаревшее | 🎯 Современное |
| Поддержка | 🕰️ 2014 год | 🆕 Активная |

---

**Сделано с ❤️ для сообщества Minecraft моддинга**

*Заменяйте старые, медленные системы. Используйте современные решения.*
