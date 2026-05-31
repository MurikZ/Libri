# Диаграммы последовательности

## SD-01: Вход в систему

```
User        LoginScreen     AuthViewModel    AuthRepository   UserDao   SessionManager
 |              |                |                |              |            |
 |--email,pass->|                |                |              |            |
 |              |--login()------>|                |              |            |
 |              |                |--login()------>|              |            |
 |              |                |                |--query()---->|            |
 |              |                |                |<--UserEntity-|            |
 |              |                |                |--saveSession()----------->|
 |              |                |<--Result.success()                         |
 |              |<--isLoading=false                                           |
 | [MainViewModel наблюдает SessionState → LoggedIn]                         |
 |<--navigate to CatalogScreen                                               |
```

## SD-02: Бронирование книги

```
User     BookDetailScreen  BookDetailVM    ReservationRepo   ReservationDao  BookInstanceDao
 |             |                |                |                  |               |
 |--tap Reserve|                |                |                  |               |
 |             |--reserve()---->|                |                  |               |
 |             |                |--reserve()---->|                  |               |
 |             |                |                |--findUserRes()--->|               |
 |             |                |                |<--null            |               |
 |             |                |                |--insert()-------->|               |
 |             |                |                |--findAvailable()---------------->|
 |             |                |                |<--instance        |               |
 |             |                |                |--update(RESERVED)--------------->|
 |             |                |<--Result.success                   |               |
 |             |<--message "Забронировано"                           |               |
```

## SD-03: Возврат книги (с просрочкой)

```
Librarian  LibrarianScreen  LibrarianVM   LoanRepository  LoanDao  BookInstanceDao  FineDao
    |            |               |               |            |            |             |
    |--tap Return|               |               |            |            |             |
    |            |--returnLoan()>|               |            |            |             |
    |            |               |--returnBook()>|            |            |             |
    |            |               |               |--findById()->|           |             |
    |            |               |               |<--LoanEntity |           |             |
    |            |               |               |--update(RETURNED)------->|             |
    |            |               |               |--update(AVAILABLE)------->|            |
    |            |               |               | [today > dueDate]         |             |
    |            |               |               |--insert(Fine)-------------------------------->|
    |            |               |<--Result.success                          |             |
    |            |<--"Книга принята"                                         |             |
```
