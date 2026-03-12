package com.example.ticketing.service;

import com.example.ticketing.domain.Event;
import com.example.ticketing.dto.EventRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private AuthenticatedUser organizer;
    private AuthenticatedUser admin;
    private AuthenticatedUser otherUser;

    @BeforeEach
    void setUp() {
        organizer = new AuthenticatedUser(1L, "organizer@test.com", "encoded", "ROLE_ORGANIZER");
        admin = new AuthenticatedUser(2L, "admin@test.com", "encoded", "ROLE_ADMIN");
        otherUser = new AuthenticatedUser(3L, "other@test.com", "encoded", "ROLE_ORGANIZER");
    }

    private void mockUser(AuthenticatedUser principal) {
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    void createEvent_Success() {
        mockUser(organizer);
        EventRequest request = new EventRequest("Concert", "Arena", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2), 500);

        when(eventRepository.save(any(Event.class))).thenAnswer(i -> {
            Event e = i.getArgument(0);
            e.setId(10L);
            return e;
        });

        Event created = eventService.createEvent(request);

        assertNotNull(created);
        assertEquals("Concert", created.getTitle());
        assertEquals(organizer.getId(), created.getOwnerId());
        assertFalse(created.getPublished());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_ByOwner_Success() {
        mockUser(organizer);
        Event existing = Event.builder().id(1L).ownerId(organizer.getId()).title("Old").venue("Old Venue")
                .startsAt(LocalDateTime.now().plusDays(2)).endsAt(LocalDateTime.now().plusDays(2).plusHours(1))
                .capacity(100).published(false).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        EventRequest request = new EventRequest("Updated", "New Venue", LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2), 200);

        Event updated = eventService.updateEvent(1L, request);

        assertEquals("Updated", updated.getTitle());
        assertEquals("New Venue", updated.getVenue());
        assertEquals(200, updated.getCapacity());
    }

    @Test
    void updateEvent_ByNonOwner_ThrowsAccessDenied() {
        mockUser(otherUser);
        Event existing = Event.builder().id(1L).ownerId(organizer.getId()).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));

        EventRequest request = new EventRequest("Hack", "Hack Venue", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1), 10);

        assertThrows(AccessDeniedException.class, () -> eventService.updateEvent(1L, request));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_ByAdmin_Success() {
        mockUser(admin);
        Event existing = Event.builder().id(1L).ownerId(organizer.getId()).title("Old").venue("V")
                .startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(1).plusHours(1))
                .capacity(50).published(false).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        EventRequest request = new EventRequest("Admin Edit", "V", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1), 50);

        Event updated = eventService.updateEvent(1L, request);
        assertEquals("Admin Edit", updated.getTitle());
    }

    @Test
    void publishEvent_Success() {
        mockUser(organizer);
        Event draft = Event.builder().id(1L).ownerId(organizer.getId()).published(false).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event published = eventService.publishEvent(1L);

        assertTrue(published.getPublished());
        verify(eventRepository).save(draft);
    }

    @Test
    void publishEvent_ByNonOwner_ThrowsAccessDenied() {
        mockUser(otherUser);
        Event draft = Event.builder().id(1L).ownerId(organizer.getId()).published(false).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(draft));

        assertThrows(AccessDeniedException.class, () -> eventService.publishEvent(1L));
    }

    @Test
    void listEvents_AsOrganizerNoFilter_ReturnsOwnEvents() {
        mockUser(organizer);
        Event ownEvent = Event.builder().id(1L).ownerId(organizer.getId()).build();
        when(eventRepository.findByOwnerId(organizer.getId())).thenReturn(List.of(ownEvent));

        List<Event> events = eventService.listEvents(null);

        assertEquals(1, events.size());
        assertEquals(organizer.getId(), events.getFirst().getOwnerId());
    }

    @Test
    void listEvents_AsAdminNoFilter_ReturnsAllEvents() {
        mockUser(admin);
        when(eventRepository.findAll()).thenReturn(List.of(
                Event.builder().id(1L).ownerId(1L).build(),
                Event.builder().id(2L).ownerId(2L).build()
        ));

        List<Event> events = eventService.listEvents(null);

        assertEquals(2, events.size());
    }

    @Test
    void listEvents_AsAdminWithOwnerFilter_FiltersCorrectly() {
        mockUser(admin);
        Event ownEvent = Event.builder().id(1L).ownerId(organizer.getId()).build();
        when(eventRepository.findByOwnerId(organizer.getId())).thenReturn(List.of(ownEvent));

        List<Event> events = eventService.listEvents(organizer.getId());

        assertEquals(1, events.size());
    }

    @Test
    void listEvents_AsOrganizerFilteringOtherOwner_ThrowsAccessDenied() {
        mockUser(organizer);

        assertThrows(AccessDeniedException.class, () -> eventService.listEvents(99L));
    }

    @Test
    void updateEvent_NotFound_ThrowsIllegalArgument() {
        mockUser(organizer);
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        EventRequest request = new EventRequest("X", "Y", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1), 10);

        assertThrows(IllegalArgumentException.class, () -> eventService.updateEvent(999L, request));
    }
}
