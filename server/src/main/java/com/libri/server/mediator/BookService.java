package com.libri.server.mediator;

import com.libri.server.dto.BookDto;
import com.libri.server.dto.BooksResponse;
import com.libri.server.dto.CreateBookRequest;
import com.libri.server.entity.Book;
import com.libri.server.entity.BookInstance;
import com.libri.server.entity.BookStatus;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.BookInstanceRepository;
import com.libri.server.foundation.BookRepository;
import com.libri.server.foundation.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepo;
    private final BookInstanceRepository instanceRepo;

    @Transactional(readOnly = true)
    public BooksResponse getBooks(int page, int size) {
        Page<Book> bookPage = bookRepo.findAll(
                PageRequest.of(page, size, Sort.by("title")));
        List<BookDto> dtos = bookPage.getContent().stream().map(BookDto::from).toList();
        return new BooksResponse(dtos, page, size,
                bookPage.getTotalElements(), bookPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public BookDto getById(Long id) {
        Book book = bookRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена: " + id));
        return BookDto.from(book);
    }

    @Transactional(readOnly = true)
    public BooksResponse search(String query, String status, int page, int size) {
        Page<Book> bookPage;
        if (query != null && !query.isBlank()) {
            bookPage = bookRepo.searchByTitleOrAuthor(
                    query, PageRequest.of(page, size, Sort.by("title")));
        } else {
            bookPage = bookRepo.findAll(PageRequest.of(page, size, Sort.by("title")));
        }

        List<BookDto> dtos = bookPage.getContent().stream()
                .map(BookDto::from)
                .filter(dto -> {
                    if (status == null || status.isBlank()) return true;
                    return status.equalsIgnoreCase(dto.getStatus() != null ? dto.getStatus().name() : "");
                })
                .toList();

        return new BooksResponse(dtos, page, size,
                bookPage.getTotalElements(), bookPage.getTotalPages());
    }

    public BookDto createBook(CreateBookRequest request) {
        if (request.getIsbn() != null && bookRepo.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("Книга с таким ISBN уже существует");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .publisher(request.getPublisher())
                .description(request.getDescription())
                .build();

        book = bookRepo.save(book);

        int count = Math.max(1, request.getInstanceCount());
        for (int i = 0; i < count; i++) {
            BookInstance instance = BookInstance.builder()
                    .book(book)
                    .inventoryNumber("INV-" + book.getId() + "-" + (i + 1))
                    .status(BookStatus.AVAILABLE)
                    .build();
            instanceRepo.save(instance);
        }

        return BookDto.from(bookRepo.findById(book.getId()).orElseThrow());
    }

    public BookDto updateBook(Long id, BookDto request) {
        Book book = bookRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена: " + id));

        book.setTitle(request.getTitle());
        if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getDescription() != null) book.setDescription(request.getDescription());

        return BookDto.from(bookRepo.save(book));
    }

    public void deleteBook(Long id) {
        if (!bookRepo.existsById(id)) {
            throw new EntityNotFoundException("Книга не найдена: " + id);
        }
        bookRepo.deleteById(id);
    }
}
