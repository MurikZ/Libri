package com.libri.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReservationRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long bookId;
}
