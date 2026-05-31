package com.libri.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateBookRequest {
    @NotBlank
    private String title;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private String description;
    private List<Long> authorIds;
    private int instanceCount = 1;
}
