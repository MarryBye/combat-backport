# AGENTS.md — Combat Backport Development Guide

Руководство для AI-агентов по структуре, механикам, правилам и рабочим процессам в проекте **Combat Backport (Minecraft 1.7.10)**.

---

## 📌 О проекте

- **Название:** Combat Backport
- **Платформа:** Minecraft 1.7.10 / Minecraft Forge `10.13.4.1614`
- **Шаблон и сборочный стек:** GTNH ExampleMod / RetroFuturaGradle (RFG) + UniMixins (Mixin 0.8.7)
- **Авторы:** `MarryBye + Gemini AI`
- **Цель:** Перенос современных боевых механик (Combat Update / 1.9+) в Minecraft 1.7.10 (кулдаун атак, сплэш-удары, индикаторы, баланс оружия, масштабирование урона и отбрасывания, совместимость с TiC).

---

## 📂 Архитектура и структура файлов

```text
combat-backport/
├── .agents/
│   └── skills/
│       ├── combat-backport-rules/
│       │   └── SKILL.md                          # Обязательные скиллы и правила для агентов
│       └── client-testing/
│           └── SKILL.md                          # Процесс тестирования и верификации клиента
├── libs/                                         # Dev-зависимости мода
│   ├── +unimixins-all-1.7.10-*.jar               # Миксины для Forge 1.7.10 (UniMixins)
│   ├── angelica-*.jar                            # Современный рендеринг и GUI опций (Angelica)
│   ├── gtnhlib-*.jar                             # Утилиты GTNHLib
│   └── hodgepodge-*.jar                          # Hodgepodge (патчи, фиксы, бэкпорты)
├── src/main/
│   ├── java/com/marrybye/combatbackport/
│   │   ├── CombatBackport.java                   # Главный класс мода (@Mod)
│   │   ├── CommonProxy.java                      # Инициализация общего прокси и сети
│   │   ├── ClientProxy.java                      # Инициализация рендера, HUD и Angelica
│   │   ├── Config.java                           # Управление конфигурацией Forge
│   │   ├── api/
│   │   │   └── ICombatPlayer.java                # Интерфейс состояния кулдауна игрока
│   │   ├── combat/
│   │   │   ├── AttackIndicatorMode.java          # Перечисление режимов (DISABLED, CROSSHAIR, HOTBAR)
│   │   │   ├── CombatManager.java                # Логика сплэш-удара sweep и масштабирования урона
│   │   │   └── WeaponRegistry.java               # Реестр скоростей оружия, эвристики и TiC
│   │   ├── client/
│   │   │   ├── AttackIndicatorRenderer.java      # Рендеринг индикатора атаки (HUD, учет F1)
│   │   │   ├── ClientMiningHandler.java          # Определение контекста копания блоков
│   │   │   ├── TooltipHandler.java               # Отображение скорости атаки в тултипах
│   │   │   ├── AngelicaIntegration.java          # Интеграция настроек видео Angelica
│   │   │   ├── gui/
│   │   │   │   └── GuiButtonAttackIndicator.java # Кнопка переключения режима индикатора
│   │   │   └── particle/
│   │   │       └── EntitySweepFX.java            # Кастомный рендер партиклов размашистого удара
│   │   ├── mixins/
│   │   │   ├── MixinEntityPlayer.java            # Миксин для кулдауна, урона, критов и свипа
│   │   │   ├── MixinMinecraft.java               # Миксин для сброса КД при ударе/промахе
│   │   │   ├── MixinPlayerControllerMP.java      # Отслеживание взаимодействия с блоками
│   │   │   ├── MixinItemRenderer.java            # Миксин для подъема оружия при КД (1.9+)
│   │   │   ├── MixinGuiOptionsRowList.java       # Добавление опции в ванильные настройки видео
│   │   │   ├── MixinGuiOptionsRowListRow.java    # Аксессор для строк таблицы настроек
│   │   │   └── MixinSodiumGameOptionPages.java   # Добавление опции в интерфейс настроек Angelica
│   │   └── network/
│   │       ├── CombatPacketHandler.java          # Обработчик сетевого канала SimpleNetworkWrapper
│   │       ├── PacketSweepAttack.java            # Пакет спавна визуального эффекта свипа
│   │       └── PacketResetCooldown.java          # Пакет сброса КД на сервере при промахе
│   └── resources/
│       ├── mcmod.info                            # Метаданные мода для FML
│       ├── mixins.combatbackport.json            # Конфигурация UniMixins
│       └── assets/combatbackport/
│           ├── lang/                             # Локализации (en_US, ru_RU)
│           ├── sounds.json                       # Регистрация звуков свип-атаки
│           ├── sounds/player/attack/sweep*.ogg   # Аутентичные звуковые файлы атаки
│           └── textures/
│               ├── gui/crosshair_*.png           # Текстуры индикатора у прицела (bg, progress, full)
│               ├── gui/hotbar_*.png              # Текстуры индикатора у хотбара (bg, progress)
│               └── particle/sweep_*.png          # Покадровая анимация sweep (8 кадров)
├── build.gradle.kts                              # GTNH Convention Plugin
├── dependencies.gradle                           # Подключение зависимостей и libs/
├── gradle.properties                             # Настройки мода (modId, modName, modGroup, mixins)
└── README.md                                     # Пользовательское описание мода
```

---

## ⚡ Обязательные правила для агентов (Agent Rules)

1. **Принятие решений и спорные моменты:**
   - Если в ходе проектирования или реализации возникает неоднозначность, спорный архитектурный выбор или выбор между разными вариантами механики — **уведомить пользователя и обсудить решение**.
   - Все остальные типовые задачи, реализацию согласованных фичей, фиксы и тесты выполнять полностью автономно.

2. **Тестирование и запуск клиента (Client Testing Workflow):**
   - По окончании работы над задачами **всегда предлагать пользователю запустить тестовый клиент разработчика** (`./gradlew runClient` или `./gradlew runClient25`).
   - Параллельно отслеживать логи в консоли.
   - В случае краша/ошибки немедленно переходить к фиксам.
   - При штатном закрытии клиента спросить пользователя, все ли прошло хорошо или нужны правки. Если все хорошо — задача считается выполненной.

3. **Повышение версии (SemVer):**
   - При команде на пуш новой версии мода обновлять версию по схеме `MAJOR.MINOR.PATCH`:
     - `MAJOR` — крупные изменения с нарушением обратной совместимости;
     - `MINOR` — добавление новых фичей / механик мода;
     - `PATCH` — мелкие багфиксы, мелкие правки и оптимизации.

4. **Оформление коммитов (Conventional Commits):**
   - Строгий формат: `<type>(<scope>): <description>` (на английском языке).
   - Примеры:
     - `fix(ui): fixed not working attack indicator`
     - `feat(combat): add attack cooldown system`
     - `refactor(config): clean up configuration properties`

5. **Краткость и емкость отчетов:**
   - После выполнения задачи давать краткий, содержательный отчет о сделанном (без лишней воды и длинных пересказов).

6. **Проверка перед началом работы:**
   - Перед началом работы всегда проверять скиллы в `.agents/skills/` и правила в `AGENTS.md`.

---

## 🔧 Полезные команды сборщика

- Сборка и компиляция: `./gradlew build`
- Форматирование кода: `./gradlew spotlessApply`
- Запуск тестового клиента: `./gradlew runClient`
- Очистка проекта: `./gradlew clean`
