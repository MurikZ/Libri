package com.libri.server.mediator;

import com.libri.server.dto.CreateReservationRequest;
import com.libri.server.dto.ReservationDto;
import com.libri.server.entity.*;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.BookRepository;
import com.libri.server.foundation.ReservationRepository;
import com.libri.server.foundation.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;

    public ReservationDto createReservation(CreateReservationRequest request) {
        if (reservationRepo.existsByUserIdAndBookIdAndStatus(
                request.getUserId(), request.getBookId(), ReservationStatus.PENDING)) {
            throw new BusinessException("Книга уже забронирована пользователем");
        }

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        Book book = bookRepo.findById(request.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена"));

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .reservationDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(3))
                .status(ReservationStatus.PENDING)
                .build();

        return ReservationDto.from(reservationRepo.save(reservation));
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Нельзя отменить бронирование со статусом: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepo.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getUserReservations(Long userId) {
        return reservationRepo.findAllByUserId(userId).stream()
                .map(ReservationDto::from).toList();
    }
}
