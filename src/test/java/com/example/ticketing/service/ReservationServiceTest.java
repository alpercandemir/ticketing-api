package com.example.ticketing.service;

import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.domain.User;
import com.example.ticketing.dto.ReservationRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").roles("ROLE_CUSTOMER").build();
        testEvent = Event.builder().id(1L).ownerId(2L).capacity(10).published(true).build();
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("user@test.com");
        lenient().when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
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
        testEvent.setCapacity(1); // Only 1 seat left
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        ReservationRequest request = new ReservationRequest(2); // Requesting 2
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
                () -> reservationService.reserveSeats(1L, request));
                
        assertEquals("Not enough capacity", ex.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }
}
