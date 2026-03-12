package com.example.ticketing.service;

import com.example.ticketing.audit.AuditLoggable;
import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.domain.User;
import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @AuditLoggable(action = "CREATE_RESERVATION", resourceType = "Reservation")
    public Reservation reserveSeats(Long eventId, ReservationRequest request) {
        User user = getCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getPublished()) {
            throw new IllegalArgumentException("Cannot reserve seats for an unpublished event");
        }

        if (event.getCapacity() < request.seats()) {
            throw new IllegalStateException("Not enough capacity");
        }

        event.setCapacity(event.getCapacity() - request.seats());
        eventRepository.save(event);

        Reservation reservation = Reservation.builder()
                .eventId(event.getId())
                .userId(user.getId())
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

        event.setCapacity(event.getCapacity() + reservation.getSeats());
        eventRepository.save(event);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        throw new AccessDeniedException("User not authenticated");
    }

    private void verifyOwnershipOrAdmin(Reservation reservation) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().contains("ROLE_ADMIN");
        boolean isOwner = reservation.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to modify this reservation");
        }
    }
}
