package com.libri.server.mediator;

import com.libri.server.dto.FineDto;
import com.libri.server.entity.*;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.FineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FineService — управление штрафами")
class FineServiceTest {

    @Mock FineRepository fineRepo;

    @InjectMocks FineService fineService;

    private Fine buildFine(boolean paid) {
        User user = User.builder().id(1L).firstName("Иван").lastName("Петров").build();
        Book book = Book.builder().id(1L).title("Книга").build();
        BookInstance instance = BookInstance.builder().id(1L).book(book).inventoryNumber("INV-001").build();
        Loan loan = Loan.builder().id(1L).user(user).bookInstance(instance)
                .loanDate(LocalDate.now().minusDays(40))
                .dueDate(LocalDate.now().minusDays(10))
                .status(LoanStatus.ACTIVE).build();
        return Fine.builder()
                .id(1L).loan(loan).user(user)
                .amount(BigDecimal.valueOf(50))
                .paid(paid)
                .calculatedDate(LocalDate.now())
                .reason("Просрочка 10 дней")
                .build();
    }

    @Test
    @DisplayName("getUserFines возвращает список штрафов пользователя")
    void getUserFines_returnsList() {
        Fine fine = buildFine(false);
        when(fineRepo.findAllByUserId(1L)).thenReturn(List.of(fine));

        List<FineDto> result = fineService.getUserFines(1L);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(50), result.get(0).getAmount());
    }

    @Test
    @DisplayName("payFine успешно оплачивает штраф")
    void payFine_success() {
        Fine fine = buildFine(false);
        when(fineRepo.findById(1L)).thenReturn(Optional.of(fine));
        when(fineRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FineDto result = fineService.payFine(1L);

        assertTrue(result.isPaid());
        verify(fineRepo).save(fine);
    }

    @Test
    @DisplayName("payFine выбрасывает исключение при повторной оплате")
    void payFine_failsWhenAlreadyPaid() {
        Fine fine = buildFine(true);
        when(fineRepo.findById(1L)).thenReturn(Optional.of(fine));

        assertThrows(BusinessException.class, () -> fineService.payFine(1L));
        verify(fineRepo, never()).save(any());
    }

    @Test
    @DisplayName("payFine выбрасывает EntityNotFoundException для несуществующего штрафа")
    void payFine_throwsWhenNotFound() {
        when(fineRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fineService.payFine(99L));
    }

    @Test
    @DisplayName("getUnpaidFines возвращает только неоплаченные штрафы")
    void getUnpaidFines_returnsOnlyUnpaid() {
        Fine unpaidFine = buildFine(false);
        when(fineRepo.findAllByUserIdAndPaidFalse(1L)).thenReturn(List.of(unpaidFine));

        List<FineDto> result = fineService.getUnpaidFines(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isPaid());
    }
}
