package com.example.ticketing.controller;

import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.dto.ReservationResponse;
import com.example.ticketing.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/events/{id}/reservations")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> reserveSeats(
            @PathVariable("id") Long eventId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ReservationRequest request) {

        ReservationResponse response = ReservationResponse.from(
                reservationService.reserveSeats(eventId, request));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/reservations/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Void> confirmReservation(@PathVariable Long id) {
        reservationService.confirmReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
