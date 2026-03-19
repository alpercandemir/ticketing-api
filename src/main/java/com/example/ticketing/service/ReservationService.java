package com.example.ticketing.service;

import com.example.ticketing.audit.AuditLoggable;
import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.domain.Role;
import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.security.AuthenticatedUser;
import com.example.ticketing.security.SecurityContextHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              EventRepository eventRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @AuditLoggable(action = "CREATE_RESERVATION", resourceType = "Reservation")
    public Reservation reserveSeats(Long eventId, ReservationRequest request) {
        AuthenticatedUser principal = SecurityContextHelper.getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.reserveSeats(request.seats());
        eventRepository.save(event);

        Reservation reservation = Reservation.builder()
                .eventId(event.getId())
                .userId(principal.getId())
                .seats(request.seats())
                .status(ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    @AuditLoggable(action = "CONFIRM_RESERVATION", resourceType = "Reservation")
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        verifyOwnershipOrAdmin(reservation);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservations can be confirmed");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    @Transactional
    @AuditLoggable(action = "CANCEL_RESERVATION", resourceType = "Reservation")
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        verifyOwnershipOrAdmin(reservation);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        Event event = eventRepository.findById(reservation.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found for reservation"));

        event.releaseSeats(reservation.getSeats());
        eventRepository.save(event);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private void verifyOwnershipOrAdmin(Reservation reservation) {
        AuthenticatedUser principal = SecurityContextHelper.getCurrentUser();
        boolean isAdmin = principal.hasRole(Role.ROLE_ADMIN);
        boolean isOwner = reservation.getUserId().equals(principal.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to modify this reservation");
        }
    }
}
