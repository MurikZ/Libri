package com.libri.server.mediator;

import com.libri.server.dto.LoanDto;
import com.libri.server.dto.ReturnResult;
import com.libri.server.entity.*;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService — бизнес-правила выдачи")
class LoanServiceTest {

    @Mock LoanRepository loanRepo;
    @Mock FineRepository fineRepo;
    @Mock BookInstanceRepository instanceRepo;
    @Mock UserRepository userRepo;

    @InjectMocks LoanService loanService;

    private User testUser;
    private BookInstance testInstance;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = Book.builder().id(1L).title("Тестовая книга").build();
        testInstance = BookInstance.builder()
                .id(1L).book(testBook)
                .inventoryNumber("INV-001")
                .status(BookStatus.AVAILABLE)
                .build();
        testUser = User.builder()
                .id(1L).email("test@lib.ru")
                .firstName("Иван").lastName("Петров")
                .role(UserRole.READER)
                .build();
    }

    // --- borrowBook ---

    @Test
    @DisplayName("BR-05: отказ при наличии неоплаченных штрафов")
    void borrowBook_failsWhenUnpaidFinesExist() {
        when(fineRepo.countByUserIdAndPaidFalse(1L)).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.borrowBook(1L, 1L));

        assertTrue(ex.getMessage().contains("штраф"));
        verify(loanRepo, never()).save(any());
    }

    @Test
    @DisplayName("BR-01: отказ при достижении лимита 5 книг")
    void borrowBook_failsWhenLimitReached() {
        when(fineRepo.countByUserIdAndPaidFalse(1L)).thenReturn(0L);
        when(loanRepo.countByUserIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.borrowBook(1L, 1L));

        assertTrue(ex.getMessage().contains("лимит"));
        verify(instanceRepo, never()).save(any());
    }

    @Test
    @DisplayName("Отказ при недоступном экземпляре (ON_LOAN)")
    void borrowBook_failsWhenInstanceUnavailable() {
        when(fineRepo.countByUserIdAndPaidFalse(1L)).thenReturn(0L);
        when(loanRepo.countByUserIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(0L);
        testInstance.setStatus(BookStatus.ON_LOAN);
        when(instanceRepo.findById(1L)).thenReturn(Optional.of(testInstance));

        assertThrows(BusinessException.class, () -> loanService.borrowBook(1L, 1L));
    }

    @Test
    @DisplayName("Успешная выдача книги")
    void borrowBook_success() {
        when(fineRepo.countByUserIdAndPaidFalse(1L)).thenReturn(0L);
        when(loanRepo.countByUserIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(2L);
        when(instanceRepo.findById(1L)).thenReturn(Optional.of(testInstance));
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(loanRepo.save(any())).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l = Loan.builder()
                    .id(100L).user(l.getUser()).bookInstance(l.getBookInstance())
                    .loanDate(l.getLoanDate()).dueDate(l.getDueDate())
                    .status(l.getStatus()).build();
            return l;
        });

        LoanDto result = loanService.borrowBook(1L, 1L);

        assertNotNull(result);
        assertEquals(BookStatus.ON_LOAN, testInstance.getStatus());
        assertEquals(LoanStatus.ACTIVE.name(), result.getStatus());
        verify(instanceRepo).save(testInstance);
    }

    @Test
    @DisplayName("Отказ для несуществующего экземпляра")
    void borrowBook_throwsWhenInstanceNotFound() {
        when(fineRepo.countByUserIdAndPaidFalse(1L)).thenReturn(0L);
        when(loanRepo.countByUserIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(0L);
        when(instanceRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.borrowBook(1L, 99L));
    }

    // --- returnBook ---

    @Test
    @DisplayName("BR-03: начисление штрафа при просрочке на 10 дней")
    void returnBook_createsFineWhenOverdue() {
        Loan loan = Loan.builder()
                .id(1L)
                .user(testUser)
                .bookInstance(testInstance)
                .loanDate(LocalDate.now().minusDays(40))
                .dueDate(LocalDate.now().minusDays(10))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepo.save(any())).thenAnswer(inv -> {
            Fine f = inv.getArgument(0);
            f = Fine.builder()
                    .id(1L).loan(loan).user(testUser)
                    .amount(f.getAmount()).paid(false)
                    .calculatedDate(f.getCalculatedDate())
                    .reason(f.getReason()).build();
            return f;
        });

        ReturnResult result = loanService.returnBook(1L);

        assertNotNull(result.getFine());
        assertEquals(0, result.getFine().getAmount().compareTo(new java.math.BigDecimal("50.00")));
        assertEquals(LoanStatus.RETURNED.name(), result.getLoan().getStatus());
        assertEquals(BookStatus.AVAILABLE, testInstance.getStatus());
    }

    @Test
    @DisplayName("Возврат без штрафа при своевременном возврате")
    void returnBook_noFineWhenOnTime() {
        Loan loan = Loan.builder()
                .id(1L)
                .user(testUser)
                .bookInstance(testInstance)
                .loanDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(20))
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReturnResult result = loanService.returnBook(1L);

        assertNull(result.getFine());
        verify(fineRepo, never()).save(any());
    }

    @Test
    @DisplayName("Отказ при повторном возврате уже возвращённой книги")
    void returnBook_failsWhenAlreadyReturned() {
        Loan loan = Loan.builder()
                .id(1L).user(testUser).bookInstance(testInstance)
                .loanDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(5))
                .returnDate(LocalDate.now().minusDays(2))
                .status(LoanStatus.RETURNED)
                .build();

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(BusinessException.class, () -> loanService.returnBook(1L));
    }
}
