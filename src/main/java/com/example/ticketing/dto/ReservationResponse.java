package com.example.ticketing.dto;

import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long eventId,
        Long userId,
        ReservationStatus status,
        Integer seats,
        LocalDateTime createdAt
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getEventId(),
                reservation.getUserId(),
                reservation.getStatus(),
                reservation.getSeats(),
                reservation.getCreatedAt()
        );
    }
}
