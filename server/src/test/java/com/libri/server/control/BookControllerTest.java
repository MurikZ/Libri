package com.libri.server.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libri.server.config.SecurityConfig;
import com.libri.server.dto.BookDto;
import com.libri.server.dto.BooksResponse;
import com.libri.server.dto.CreateBookRequest;
import com.libri.server.mediator.BookService;
import com.libri.server.security.JwtAuthFilter;
import com.libri.server.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("BookController — REST тесты")
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BookService bookService;
    @MockBean JwtTokenProvider tokenProvider;

    @Test
    @WithMockUser(roles = "READER")
    @DisplayName("GET /api/books возвращает 200 со списком")
    void getBooks_returns200() throws Exception {
        BookDto dto = new BookDto();
        dto.setId(1L);
        dto.setTitle("Тест");
        BooksResponse response = new BooksResponse(List.of(dto), 0, 20, 1L, 1);

        when(bookService.getBooks(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].title").value("Тест"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("POST /api/books создаёт книгу и возвращает 201")
    void createBook_returns201() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Новая книга");

        BookDto dto = new BookDto();
        dto.setId(5L);
        dto.setTitle("Новая книга");

        when(bookService.createBook(any())).thenReturn(dto);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Новая книга"));
    }

    @Test
    @WithMockUser(roles = "READER")
    @DisplayName("POST /api/books возвращает 403 для READER")
    void createBook_forbiddenForReader() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Книга");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/books без авторизации возвращает 403")
    void getBooks_unauthorizedReturns403() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "READER")
    @DisplayName("GET /api/books/search возвращает результаты поиска")
    void searchBooks_returns200() throws Exception {
        BooksResponse response = new BooksResponse(List.of(), 0, 20, 0L, 0);
        when(bookService.search(any(), any(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/books/search").param("q", "Толстой"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray());
    }
}
