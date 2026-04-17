package com.example.ticketing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.dto.ReservationResponse;
import com.example.ticketing.service.ReservationService;

import jakarta.validation.Valid;

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
