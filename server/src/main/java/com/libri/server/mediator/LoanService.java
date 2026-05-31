package com.libri.server.mediator;

import com.libri.server.dto.BorrowRequest;
import com.libri.server.dto.FineDto;
import com.libri.server.dto.LoanDto;
import com.libri.server.dto.ReturnResult;
import com.libri.server.entity.*;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRepository loanRepo;
    private final FineRepository fineRepo;
    private final BookInstanceRepository instanceRepo;
    private final UserRepository userRepo;

    public LoanDto borrowBook(Long userId, Long bookInstanceId) {
        // BR-05: проверка неоплаченных штрафов
        long unpaidFines = fineRepo.countByUserIdAndPaidFalse(userId);
        if (unpaidFines > 0) {
            throw new BusinessException("Есть неоплаченные штрафы: " + unpaidFines);
        }

        // BR-01: лимит 5 книг
        long activeLoans = loanRepo.countByUserIdAndStatus(userId, LoanStatus.ACTIVE);
        if (activeLoans >= 5) {
            throw new BusinessException("Достигнут лимит: нельзя взять более 5 книг одновременно");
        }

        BookInstance instance = instanceRepo.findById(bookInstanceId)
                .orElseThrow(() -> new EntityNotFoundException("Экземпляр не найден: " + bookInstanceId));

        if (instance.getStatus() != BookStatus.AVAILABLE) {
            throw new BusinessException("Экземпляр недоступен: " + instance.getStatus());
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Loan loan = Loan.builder()
                .user(user)
                .bookInstance(instance)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status(LoanStatus.ACTIVE)
                .build();

        instance.setStatus(BookStatus.ON_LOAN);
        instanceRepo.save(instance);

        return LoanDto.from(loanRepo.save(loan));
    }

    public ReturnResult returnBook(Long loanId) {
        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Выдача не найдена: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessException("Книга уже возвращена");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        loan.getBookInstance().setStatus(BookStatus.AVAILABLE);
        instanceRepo.save(loan.getBookInstance());

        // BR-03: штраф за просрочку (5 руб./день)
        FineDto fineDto = null;
        if (LocalDate.now().isAfter(loan.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
            Fine fine = Fine.builder()
                    .loan(loan)
                    .user(loan.getUser())
                    .amount(BigDecimal.valueOf(overdueDays * 5L))
                    .paid(false)
                    .calculatedDate(LocalDate.now())
                    .reason("Просрочка " + overdueDays + " дней")
                    .build();
            fineDto = FineDto.from(fineRepo.save(fine));
        }

        return new ReturnResult(LoanDto.from(loanRepo.save(loan)), fineDto);
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getUserLoans(Long userId) {
        return loanRepo.findAllByUserId(userId).stream().map(LoanDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDto> getActiveLoans() {
        return loanRepo.findAllByStatus(LoanStatus.ACTIVE).stream().map(LoanDto::from).toList();
    }
}
