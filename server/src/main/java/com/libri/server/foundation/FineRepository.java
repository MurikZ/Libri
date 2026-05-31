package com.libri.server.foundation;

import com.libri.server.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    long countByUserIdAndPaidFalse(Long userId);
    List<Fine> findAllByUserId(Long userId);
    List<Fine> findAllByUserIdAndPaidFalse(Long userId);
}
