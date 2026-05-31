package com.libri.server.control;

import com.libri.server.dto.BookDto;
import com.libri.server.dto.BooksResponse;
import com.libri.server.dto.CreateBookRequest;
import com.libri.server.mediator.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Книги")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Список книг с пагинацией")
    public BooksResponse getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookService.getBooks(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Книга по ID")
    public BookDto getBook(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск книг по названию/автору")
    public BooksResponse searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookService.search(q, status, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать книгу [LIBRARIAN, ADMIN]")
    public BookDto createBook(@Valid @RequestBody CreateBookRequest request) {
        return bookService.createBook(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить книгу [LIBRARIAN, ADMIN]")
    public BookDto updateBook(@PathVariable Long id, @Valid @RequestBody BookDto request) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить книгу [ADMIN]")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
