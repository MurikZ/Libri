package com.libri.server.dto;

import com.libri.server.entity.Fine;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FineDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private Long loanId;
    private BigDecimal amount;
    private boolean paid;
    private LocalDate calculatedDate;
    private String reason;

    public static FineDto from(Fine fine) {
        FineDto dto = new FineDto();
        dto.setId(fine.getId());
        dto.setUserId(fine.getUser().getId());
        dto.setUserFullName(fine.getUser().getFirstName() + " " + fine.getUser().getLastName());
        dto.setLoanId(fine.getLoan().getId());
        dto.setAmount(fine.getAmount());
        dto.setPaid(fine.getPaid());
        dto.setCalculatedDate(fine.getCalculatedDate());
        dto.setReason(fine.getReason());
        return dto;
    }
}
