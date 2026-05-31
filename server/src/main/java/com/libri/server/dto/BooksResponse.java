package com.libri.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BooksResponse {
    private List<BookDto> books;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
