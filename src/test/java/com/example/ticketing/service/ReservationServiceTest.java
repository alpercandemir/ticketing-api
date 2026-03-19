package com.example.ticketing.service;

import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReservationService reservationService;

    private AuthenticatedUser testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new AuthenticatedUser(1L, "user@test.com", "encoded", "ROLE_CUSTOMER");
        testEvent = Event.builder().id(1L).ownerId(2L).capacity(10).published(true).build();
    }

    private void mockSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    void reserveSeats_Success() {
        mockSecurityContext();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReservationRequest request = new ReservationRequest(2);
        Reservation reservation = reservationService.reserveSeats(1L, request);

        assertNotNull(reservation);
        assertEquals(8, testEvent.getCapacity());
        assertEquals(100L, reservation.getId());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());

        verify(eventRepository).save(testEvent);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserveSeats_NotEnoughCapacity() {
        mockSecurityContext();
        testEvent.setCapacity(1);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        ReservationRequest request = new ReservationRequest(2);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservationService.reserveSeats(1L, request));

        assertEquals("Not enough capacity", ex.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void reserveSeats_UnpublishedEvent_Throws() {
        mockSecurityContext();
        testEvent.setPublished(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        ReservationRequest request = new ReservationRequest(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.reserveSeats(1L, request));

        assertEquals("Cannot reserve seats for an unpublished event", ex.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }
}
