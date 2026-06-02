# База данных

## ER-диаграмма (Mermaid)

```mermaid
erDiagram
  users {
    int id PK
    string email
    string passwordHash
    string firstName
    string lastName
    string phone
    string role
    date registrationDate
    string city
  }
  books {
    int id PK
    string title
    string description
    int publicationYear
    string isbn
    string publisher
  }
  authors {
    int id PK
    string firstName
    string lastName
  }
  book_instances {
    int id PK
    int bookId FK
    string inventoryNumber
    string status
    string location
  }
  loans {
    int id PK
    int userId FK
    int bookInstanceId FK
    date loanDate
    date dueDate
    date returnDate
    string status
  }
  reservations {
    int id PK
    int userId FK
    int bookId FK
    date reservationDate
    date expiryDate
    string status
  }
  fines {
    int id PK
    int userId FK
    int loanId FK
    float amount
    string reason
    bool paid
    date calculatedDate
  }
  book_author_cross_ref {
    int bookId FK
    int authorId FK
  }

  users ||--o{ loans : "берёт"
  users ||--o{ reservations : "бронирует"
  users ||--o{ fines : "имеет"
  books ||--o{ book_instances : "содержит"
  books ||--o{ reservations : "бронируется"
  books }o--o{ authors : "написана"
  book_author_cross_ref }o--|| books : "bookId"
  book_author_cross_ref }o--|| authors : "authorId"
  book_instances ||--o{ loans : "выдаётся"
  loans ||--o| fines : "порождает"
```

## Описание таблиц

| Таблица | Назначение |
|---|---|
| `users` | Пользователи системы (читатели, библиотекари, администраторы) |
| `books` | Библиографические данные книг |
| `authors` | Авторы (М:N с books) |
| `book_author_cross_ref` | Связь книг и авторов (many-to-many) |
| `book_instances` | Физические экземпляры книг |
| `loans` | Записи о выдачах |
| `reservations` | Брони на книги (срок 3 дня) |
| `fines` | Штрафы за просрочку (5 руб./день) |

## Бизнес-правила

- Лимит выдач на читателя: **5 книг**
- Срок бронирования: **3 дня**
- Срок выдачи: **14 дней**
- Штраф за просрочку: **5 руб./день**
- Нельзя взять книгу при наличии неоплаченных штрафов

## Диаграмма

![ER-диаграмма](../images/er-diagram.png)
