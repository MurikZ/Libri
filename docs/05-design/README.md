# Этап 4: Детальное проектирование

**Недели:** 9–10 | **Вес:** 10%

## UC-004: Взять книгу

Последовательность: LibrarianScreen → LoanViewModel → LoanRepository → проверка штрафов (FineDao) → проверка лимита (LoanDao) → атомарная транзакция: создание Loan + обновление статуса BookInstance на ON_LOAN.

![Диаграмма UC-004](../images/sequence-uc004.png)

## UC-005: Вернуть книгу

Последовательность: LibrarianScreen → LoanViewModel → LoanRepository → обновление Loan (RETURNED) + обновление BookInstance (AVAILABLE). Если returnDate > dueDate — автоматически создаётся Fine = дни × 5 руб. Все операции атомарны (@Transaction).

![Диаграмма UC-005](../images/sequence-uc005.png)

## UC-011: Забронировать книгу

Последовательность: BookDetailScreen → CatalogViewModel → ReservationRepository → проверка активных броней → создание Reservation(PENDING) с expiryDate = today + 3 дня + обновление статуса книги на RESERVED.

![Диаграмма UC-011](../images/sequence-uc011.png)
