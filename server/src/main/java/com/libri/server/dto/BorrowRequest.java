package com.libri.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long bookInstanceId;
}
