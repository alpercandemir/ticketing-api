package com.example.ticketing.controller;

import com.example.ticketing.domain.Event;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.support.TestAuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    private Long publishedEventId;

    @BeforeEach
    void setUp() {
        Event event = Event.builder()
                .ownerId(2L)
                .title("Security Test Event")
                .venue("Test Venue")
                .startsAt(LocalDateTime.now().plusDays(1))
                .endsAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .capacity(100)
                .published(true)
                .build();
        publishedEventId = eventRepository.save(event).getId();
    }

    // --- Public endpoints should be accessible without auth ---

    @Test
    void publicDiscoveryEndpoint_NoAuth_Returns200() throws Exception {
        mockMvc.perform(get("/api/events/public"))
                .andExpect(status().isOk());
    }

    @Test
    void authEndpoints_NoAuth_Returns200() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    // --- Protected endpoints should reject unauthenticated requests ---

    @Test
    void createEvent_NoAuth_Returns401() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listEvents_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reserveSeats_NoAuth_Returns401() throws Exception {
        mockMvc.perform(post("/api/events/" + publishedEventId + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "sec-test-1")
                        .content("{\"seats\":1}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Role-based access: CUSTOMER cannot create events ---

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void createEvent_AsCustomer_Returns403() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void publishEvent_AsCustomer_Returns403() throws Exception {
        mockMvc.perform(post("/api/events/" + publishedEventId + "/publish"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void listEvents_AsCustomer_Returns403() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isForbidden());
    }

    // --- Role-based access: ORGANIZER can create events ---

    @Test
    @TestAuthenticatedUser(id = 2, email = "organizer@example.com", roles = "ROLE_ORGANIZER")
    void createEvent_AsOrganizer_Returns201() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @TestAuthenticatedUser(id = 2, email = "organizer@example.com", roles = "ROLE_ORGANIZER")
    void listEvents_AsOrganizer_Returns200() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    // --- Role-based access: ADMIN has full access ---

    @Test
    @TestAuthenticatedUser(id = 1, email = "admin@example.com", roles = "ROLE_ADMIN")
    void createEvent_AsAdmin_Returns201() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson()))
                .andExpect(status().isCreated());
    }

    // --- Role-based access: CUSTOMER can reserve seats ---

    @Test
    @TestAuthenticatedUser(id = 3, email = "customer@example.com", roles = "ROLE_CUSTOMER")
    void reserveSeats_AsCustomer_Returns201() throws Exception {
        mockMvc.perform(post("/api/events/" + publishedEventId + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "sec-test-reserve-" + System.nanoTime())
                        .content("{\"seats\":1}"))
                .andExpect(status().isCreated());
    }

    // --- ORGANIZER cannot reserve seats ---

    @Test
    @WithMockUser(username = "organizer@example.com", roles = "ORGANIZER")
    void reserveSeats_AsOrganizer_Returns403() throws Exception {
        mockMvc.perform(post("/api/events/" + publishedEventId + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "sec-test-org-1")
                        .content("{\"seats\":1}"))
                .andExpect(status().isForbidden());
    }

    // --- Idempotency-Key is required for reservations ---

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void reserveSeats_MissingIdempotencyKey_Returns400() throws Exception {
        mockMvc.perform(post("/api/events/" + publishedEventId + "/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seats\":1}"))
                .andExpect(status().isBadRequest());
    }

    private String eventJson() {
        return """
            {
                "title": "Test Event",
                "venue": "Test Venue",
                "startsAt": "%s",
                "endsAt": "%s",
                "capacity": 100
            }
            """.formatted(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(5).plusHours(2));
    }
}
