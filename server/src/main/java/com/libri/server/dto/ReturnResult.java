package com.libri.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReturnResult {
    private LoanDto loan;
    private FineDto fine;
}
