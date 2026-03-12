package com.example.ticketing.controller;

import com.example.ticketing.dto.EventRequest;
import com.example.ticketing.dto.EventResponse;
import com.example.ticketing.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = EventResponse.from(eventService.createEvent(request));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {

        return ResponseEntity.ok(EventResponse.from(eventService.updateEvent(id, request)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.publishEvent(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> listEvents(
            @RequestParam(required = false) Long ownerId) {

        List<EventResponse> events = eventService.listEvents(ownerId).stream()
                .map(EventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/public")
    public ResponseEntity<List<EventResponse>> discoverEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String q) {

        List<EventResponse> events = eventService.discoverEvents(from, to, q).stream()
                .map(EventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }
}
