package com.libri.server.dto;

import com.libri.server.entity.Book;
import com.libri.server.entity.BookStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class BookDto {
    private Long id;
    @NotBlank
    private String title;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private String description;
    private List<AuthorDto> authors;
    private int totalInstances;
    private int availableInstances;
    private BookStatus status;

    public static BookDto from(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setPublisher(book.getPublisher());
        dto.setDescription(book.getDescription());
        if (book.getAuthors() != null) {
            dto.setAuthors(book.getAuthors().stream().map(AuthorDto::from).toList());
        }
        if (book.getInstances() != null) {
            dto.setTotalInstances(book.getInstances().size());
            long available = book.getInstances().stream()
                    .filter(i -> i.getStatus() == BookStatus.AVAILABLE).count();
            dto.setAvailableInstances((int) available);
            dto.setStatus(available > 0 ? BookStatus.AVAILABLE : BookStatus.ON_LOAN);
        }
        return dto;
    }
}
