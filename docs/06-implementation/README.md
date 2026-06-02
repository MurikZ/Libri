# Этап 5–6: Реализация и рефакторинг

**Недели:** 11–14 | **Вес:** 25%

## Структура проекта

Проект разделён на два модуля: `app/` (Android, Kotlin) и `server/` (Java 17, Spring Boot). Android-модуль организован по слоям PCMEF: presentation → repository → data/local + data/remote.

## Паттерны рефакторинга

**Data Mapper** — преобразование между слоями через функции-расширения Kotlin: `BookEntity.toDomain()`, `BookDto.toEntity()`. Бизнес-слой не знает о структуре БД.

**Identity Map** — кэш объектов в `BookRepository` через `LinkedHashMap` с LRU-стратегией (максимум 200 объектов). Повторные запросы одной книги не генерируют SQL-запросы.

## Результаты тестирования

| Класс теста | Тестов | Прошло | Покрытие |
|---|---|---|---|
| LoanServiceTest | 12 | 12 ✓ | 74% |
| BookServiceTest | 8 | 8 ✓ | 62% |
| FineServiceTest | 6 | 6 ✓ | 100% |
| AuthServiceTest | 5 | 5 ✓ | 58% |
| LoanDaoIntegrationTest | 7 | 7 ✓ | 81% |
| **ИТОГО** | **38** | **38 ✓** | **69%** |
