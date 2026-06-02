# Этап 2: Архитектурное проектирование

**Недели:** 5–6 | **Вес:** 10%

## Диаграмма пакетов PCMEF

Архитектура разделена на два компонента. Мобильный клиент (Android): Presentation (Compose экраны) → State Management (ViewModel) → API Client (Retrofit + JWT) → Local Cache (Room). Сервер (Spring Boot): Control (Controllers) → Mediator (Services) → Entity (JPA) → Foundation (Repositories) → PostgreSQL. Клиент взаимодействует с сервером через REST API по HTTPS.

![PCMEF диаграмма](../images/pcmef-diagram.png)

## Диаграмма классов проектирования

Детализирует связи между слоями: CatalogScreen зависит от CatalogViewModel, который использует BookRepository. BookRepository работает одновременно с BookApi (сеть) и BookDao (кэш). Реализован паттерн Identity Map через LinkedHashMap в репозитории.

![Диаграмма классов](../images/class-diagram.png)

## Интерфейсы и ADR

[interfaces.md](interfaces.md) — контракты ILoanService, IBookService, ILoanRepository. Архитектурные решения: Kotlin+Compose, Room, Hilt, StateFlow, Spring Boot, JWT.
