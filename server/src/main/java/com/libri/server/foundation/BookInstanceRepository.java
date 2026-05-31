package com.libri.server.foundation;

import com.libri.server.entity.BookInstance;
import com.libri.server.entity.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookInstanceRepository extends JpaRepository<BookInstance, Long> {
    List<BookInstance> findAllByBookIdAndStatus(Long bookId, BookStatus status);
    Optional<BookInstance> findByInventoryNumber(String inventoryNumber);
    List<BookInstance> findAllByBookId(Long bookId);
}
