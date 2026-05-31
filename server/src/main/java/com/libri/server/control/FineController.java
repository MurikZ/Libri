package com.libri.server.control;

import com.libri.server.dto.FineDto;
import com.libri.server.mediator.FineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
@Tag(name = "Штрафы")
@SecurityRequirement(name = "bearerAuth")
public class FineController {

    private final FineService fineService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Штрафы пользователя")
    public List<FineDto> getUserFines(@PathVariable Long userId) {
        return fineService.getUserFines(userId);
    }

    @GetMapping("/user/{userId}/unpaid")
    @Operation(summary = "Неоплаченные штрафы пользователя")
    public List<FineDto> getUnpaidFines(@PathVariable Long userId) {
        return fineService.getUnpaidFines(userId);
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Оплатить штраф")
    public FineDto payFine(@PathVariable Long id) {
        return fineService.payFine(id);
    }
}
