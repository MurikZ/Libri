package com.libri.server.control;

import com.libri.server.dto.BorrowRequest;
import com.libri.server.dto.LoanDto;
import com.libri.server.dto.ReturnResult;
import com.libri.server.mediator.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Выдачи")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Выдать книгу читателю [LIBRARIAN, ADMIN]")
    public LoanDto borrowBook(@Valid @RequestBody BorrowRequest request) {
        return loanService.borrowBook(request.getUserId(), request.getBookInstanceId());
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Вернуть книгу [LIBRARIAN, ADMIN]")
    public ReturnResult returnBook(@PathVariable Long id) {
        return loanService.returnBook(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Выдачи пользователя")
    public List<LoanDto> getUserLoans(@PathVariable Long userId) {
        return loanService.getUserLoans(userId);
    }

    @GetMapping("/active")
    @Operation(summary = "Все активные выдачи [LIBRARIAN, ADMIN]")
    public List<LoanDto> getActiveLoans() {
        return loanService.getActiveLoans();
    }
}
