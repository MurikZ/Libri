package com.libri.server.foundation;

import com.libri.server.entity.Loan;
import com.libri.server.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByUserIdAndStatus(Long userId, LoanStatus status);
    List<Loan> findAllByUserId(Long userId);
    List<Loan> findAllByStatus(LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :today")
    List<Loan> findAllOverdue(@Param("today") LocalDate today);
}
