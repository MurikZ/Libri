package com.libri.server.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libri.server.config.SecurityConfig;
import com.libri.server.dto.BorrowRequest;
import com.libri.server.dto.LoanDto;
import com.libri.server.dto.ReturnResult;
import com.libri.server.mediator.LoanService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
@DisplayName("LoanController — REST тесты")
class LoanControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean LoanService loanService;
    @MockBean JwtTokenProvider tokenProvider;

    private LoanDto buildLoanDto() {
        LoanDto dto = new LoanDto();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setBookId(1L);
        dto.setBookTitle("Тест");
        dto.setLoanDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(30));
        dto.setStatus("ACTIVE");
        return dto;
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("POST /api/loans выдаёт книгу и возвращает 201")
    void borrowBook_returns201() throws Exception {
        BorrowRequest req = new BorrowRequest();
        req.setUserId(1L);
        req.setBookInstanceId(1L);

        when(loanService.borrowBook(1L, 1L)).thenReturn(buildLoanDto());

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.bookTitle").value("Тест"));
    }

    @Test
    @WithMockUser(roles = "READER")
    @DisplayName("POST /api/loans запрещён для READER")
    void borrowBook_forbiddenForReader() throws Exception {
        BorrowRequest req = new BorrowRequest();
        req.setUserId(1L);
        req.setBookInstanceId(1L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("PUT /api/loans/{id}/return возвращает книгу")
    void returnBook_returns200() throws Exception {
        LoanDto loanDto = buildLoanDto();
        loanDto.setStatus("RETURNED");
        ReturnResult result = new ReturnResult(loanDto, null);

        when(loanService.returnBook(anyLong())).thenReturn(result);

        mockMvc.perform(put("/api/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loan.status").value("RETURNED"))
                .andExpect(jsonPath("$.fine").isEmpty());
    }

    @Test
    @WithMockUser(roles = "READER")
    @DisplayName("GET /api/loans/user/{userId} возвращает список выдач")
    void getUserLoans_returns200() throws Exception {
        when(loanService.getUserLoans(1L)).thenReturn(List.of(buildLoanDto()));

        mockMvc.perform(get("/api/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
