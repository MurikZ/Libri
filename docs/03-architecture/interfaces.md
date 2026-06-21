# Интерфейсы между слоями PCMEF

## Control → Mediator

```java
public interface ILoanService {
    LoanDto borrowBook(Long userId, Long bookInstanceId);
    ReturnResult returnBook(Long loanId);
    List<LoanDto> getActiveLoansByUser(Long userId);
    ReservationDto reserveBook(Long userId, Long bookId);
}

public interface IBookService {
    List<BookDto> getAllBooks(int page, int size);
    BookDto getBookById(Long id);
    BookDto createBook(CreateBookRequest request);
    BookDto updateBook(Long id, CreateBookRequest request);
    void deleteBook(Long id);
    List<BookDto> searchBooks(String query);
}
```

## Mediator → Foundation

```java
public interface ILoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);
    long countByUserIdAndStatus(Long userId, LoanStatus status);
}

public interface IBookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    boolean existsByIsbn(String isbn);
}
```

## Правило зависимостей

Зависимости направлены строго сверху вниз:
`Presentation → Control → Mediator → Entity → Foundation`

Вышележащие слои зависят от интерфейсов, а не от конкретных реализаций.
Это обеспечивает тестируемость каждого слоя в изоляции через моки.