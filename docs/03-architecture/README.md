# Архитектура PCMEF

Приложение Libri реализует **адаптированную архитектуру PCMEF** для мобильной разработки.

## Слои архитектуры

| Слой PCMEF | Реализация в Android | Пакет |
|---|---|---|
| **Presentation** | Composable-экраны | `presentation/` |
| **Control (State Management)** | ViewModel + StateFlow | `presentation/*/ViewModel` |
| **Mediator** | Repository (бизнес-логика) | `repository/` |
| **Entity** | Domain Models | `domain/model/` |
| **Foundation** | Room DAO + SQLite | `data/` |

## Правило зависимостей

```
Presentation → ViewModel → Repository → DAO → Room/SQLite
```

Зависимости направлены строго вниз. Нижние слои не знают о верхних.

## PlantUML — Диаграмма пакетов

```plantuml
@startuml
title Libri — Архитектура PCMEF (Mobile)

package "Presentation" {
  rectangle "CatalogScreen"
  rectangle "BookDetailScreen"
  rectangle "LoginScreen"
  rectangle "RegisterScreen"
  rectangle "MyBooksScreen"
  rectangle "ProfileScreen"
  rectangle "LibrarianScreen"
}

package "StateManagement (Control)" {
  rectangle "CatalogViewModel"
  rectangle "BookDetailViewModel"
  rectangle "AuthViewModel"
  rectangle "LoansViewModel"
  rectangle "ProfileViewModel"
  rectangle "LibrarianViewModel"
  rectangle "MainViewModel"
}

package "Repository (Mediator)" {
  rectangle "AuthRepository"
  rectangle "BookRepository"
  rectangle "LoanRepository"
  rectangle "ReservationRepository"
  rectangle "FineRepository"
  rectangle "SessionManager"
  rectangle "DataPreloader"
}

package "Foundation (Room)" {
  rectangle "UserDao"
  rectangle "BookDao"
  rectangle "BookInstanceDao"
  rectangle "LoanDao"
  rectangle "ReservationDao"
  rectangle "FineDao"
}

database "SQLite (libri_db)" as DB

Presentation --> StateManagement
StateManagement --> Repository
Repository --> Foundation
Foundation --> DB
@enduml
```

## Потоки данных

### Вход пользователя

```
LoginScreen → AuthViewModel.login()
  → AuthRepository.login()
    → UserDao.login() [Room query]
    → SessionManager.saveSession() [DataStore]
  ← Result<UserEntity>
← SessionState.LoggedIn → навигация на Main
```

### Бронирование книги

```
BookDetailScreen → BookDetailViewModel.reserve()
  → ReservationRepository.reserve()
    → ReservationDao.insert() [Room]
    → BookInstanceDao.update(RESERVED) [Room]
  ← Result<Unit>
← StateFlow обновляется → UI перерисовывается
```

## Диаграммы

![PCMEF диаграмма](../images/pcmef-diagram.png)

![Диаграмма классов](../images/class-diagram.png)
