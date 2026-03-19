package com.example.ticketing.service;

import com.example.ticketing.audit.AuditLoggable;
import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Role;
import com.example.ticketing.dto.EventRequest;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.security.AuthenticatedUser;
import com.example.ticketing.security.SecurityContextHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    @AuditLoggable(action = "CREATE_EVENT", resourceType = "Event")
    public Event createEvent(EventRequest request) {
        AuthenticatedUser principal = SecurityContextHelper.getCurrentUser();

        Event event = Event.builder()
                .ownerId(principal.getId())
                .title(request.title())
                .venue(request.venue())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .capacity(request.capacity())
                .published(false)
                .build();

        return eventRepository.save(event);
    }

    @Transactional
    @AuditLoggable(action = "UPDATE_EVENT", resourceType = "Event")
    public Event updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        verifyOwnershipOrAdmin(event);

        event.updateDetails(request.title(), request.venue(), request.startsAt(),
                request.endsAt(), request.capacity());

        return eventRepository.save(event);
    }

    @Transactional
    @AuditLoggable(action = "PUBLISH_EVENT", resourceType = "Event")
    public Event publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        verifyOwnershipOrAdmin(event);

        event.publish();
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> listEvents(Long ownerId) {
        AuthenticatedUser principal = SecurityContextHelper.getCurrentUser();
        boolean isAdmin = principal.hasRole(Role.ROLE_ADMIN);

        if (ownerId != null) {
            if (!isAdmin && !principal.getId().equals(ownerId)) {
                throw new AccessDeniedException("You can only list your own events");
            }
            return eventRepository.findByOwnerId(ownerId);
        }

        if (isAdmin) {
            return eventRepository.findAll();
        }
        return eventRepository.findByOwnerId(principal.getId());
    }

    @Transactional(readOnly = true)
    public List<Event> discoverEvents(LocalDateTime from, LocalDateTime to, String q) {
        return eventRepository.discoverEvents(from, to, q);
    }

    private void verifyOwnershipOrAdmin(Event event) {
        AuthenticatedUser principal = SecurityContextHelper.getCurrentUser();
        boolean isAdmin = principal.hasRole(Role.ROLE_ADMIN);
        boolean isOwner = event.getOwnerId().equals(principal.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to modify this event");
        }
    }
}
