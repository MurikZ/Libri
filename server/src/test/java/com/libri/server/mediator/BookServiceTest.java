package com.libri.server.mediator;

import com.libri.server.dto.BookDto;
import com.libri.server.dto.BooksResponse;
import com.libri.server.dto.CreateBookRequest;
import com.libri.server.entity.Book;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.BookInstanceRepository;
import com.libri.server.foundation.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService — управление каталогом")
class BookServiceTest {

    @Mock BookRepository bookRepo;
    @Mock BookInstanceRepository instanceRepo;

    @InjectMocks BookService bookService;

    @Test
    @DisplayName("getBooks возвращает страницу книг")
    void getBooks_returnsPaginatedList() {
        Book book = Book.builder().id(1L).title("Тест").build();
        when(bookRepo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(book)));

        BooksResponse response = bookService.getBooks(0, 20);

        assertEquals(1, response.getBooks().size());
        assertEquals("Тест", response.getBooks().get(0).getTitle());
    }

    @Test
    @DisplayName("getById выбрасывает EntityNotFoundException для несуществующей книги")
    void getById_throwsWhenNotFound() {
        when(bookRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getById(99L));
    }

    @Test
    @DisplayName("createBook выбрасывает исключение при дублирующемся ISBN")
    void createBook_failsOnDuplicateIsbn() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Книга");
        req.setIsbn("1234567890123");
        when(bookRepo.existsByIsbn("1234567890123")).thenReturn(true);

        assertThrows(BusinessException.class, () -> bookService.createBook(req));
        verify(bookRepo, never()).save(any());
    }

    @Test
    @DisplayName("createBook сохраняет книгу и создаёт экземпляры")
    void createBook_savesBookAndInstances() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Новая книга");
        req.setIsbn("9781234567890");
        req.setInstanceCount(2);

        Book savedBook = Book.builder().id(10L).title("Новая книга").isbn("9781234567890").build();
        when(bookRepo.existsByIsbn(any())).thenReturn(false);
        when(bookRepo.save(any())).thenReturn(savedBook);
        when(bookRepo.findById(10L)).thenReturn(Optional.of(savedBook));

        BookDto result = bookService.createBook(req);

        assertNotNull(result);
        assertEquals("Новая книга", result.getTitle());
        verify(instanceRepo, times(2)).save(any());
    }

    @Test
    @DisplayName("deleteBook выбрасывает исключение для несуществующей книги")
    void deleteBook_throwsWhenNotFound() {
        when(bookRepo.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> bookService.deleteBook(99L));
        verify(bookRepo, never()).deleteById(any());
    }
}
