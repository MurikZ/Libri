package com.libri.server.mediator;

import com.libri.server.dto.FineDto;
import com.libri.server.entity.Fine;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.FineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FineService {

    private final FineRepository fineRepo;

    @Transactional(readOnly = true)
    public List<FineDto> getUserFines(Long userId) {
        return fineRepo.findAllByUserId(userId).stream().map(FineDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FineDto> getUnpaidFines(Long userId) {
        return fineRepo.findAllByUserIdAndPaidFalse(userId).stream().map(FineDto::from).toList();
    }

    public FineDto payFine(Long fineId) {
        Fine fine = fineRepo.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("Штраф не найден: " + fineId));

        if (fine.getPaid()) {
            throw new BusinessException("Штраф уже оплачен");
        }

        fine.setPaid(true);
        return FineDto.from(fineRepo.save(fine));
    }
}
