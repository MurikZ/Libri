package com.libri.server.dto;

import com.libri.server.entity.Loan;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long bookInstanceId;
    private String inventoryNumber;
    private Long bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private boolean overdue;

    public static LoanDto from(Loan loan) {
        LoanDto dto = new LoanDto();
        dto.setId(loan.getId());
        dto.setUserId(loan.getUser().getId());
        dto.setUserFullName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
        dto.setBookInstanceId(loan.getBookInstance().getId());
        dto.setInventoryNumber(loan.getBookInstance().getInventoryNumber());
        dto.setBookId(loan.getBookInstance().getBook().getId());
        dto.setBookTitle(loan.getBookInstance().getBook().getTitle());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setStatus(loan.getStatus().name());
        dto.setOverdue(loan.getReturnDate() == null && LocalDate.now().isAfter(loan.getDueDate()));
        return dto;
    }
}
