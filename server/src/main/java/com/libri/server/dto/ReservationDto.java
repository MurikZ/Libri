package com.libri.server.dto;

import com.libri.server.entity.Reservation;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private String status;

    public static ReservationDto from(Reservation r) {
        ReservationDto dto = new ReservationDto();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setBookId(r.getBook().getId());
        dto.setBookTitle(r.getBook().getTitle());
        dto.setReservationDate(r.getReservationDate());
        dto.setExpiryDate(r.getExpiryDate());
        dto.setStatus(r.getStatus().name());
        return dto;
    }
}
