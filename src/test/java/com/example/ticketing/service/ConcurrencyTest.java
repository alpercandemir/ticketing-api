package com.example.ticketing.service;

import com.example.ticketing.domain.Event;
import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.repository.EventRepository;
import com.example.ticketing.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testOversellingProtectionWithOptimisticLocking() throws InterruptedException {
        // Setup Event with Capacity 1
        Event event = Event.builder()
                .ownerId(2L) // Organizer from seed data
                .title("Concurrency Test Event")
                .venue("Test Venue")
                .startsAt(LocalDateTime.now().plusDays(1))
                .endsAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .capacity(1) // Only 1 seat
                .published(true)
                .build();
        
        event = eventRepository.save(event);
        final Long eventId = event.getId();

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger failedReservations = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    // Simulate concurrent transaction
                    Boolean reserved = transactionTemplate.execute(status -> {
                        Event e = eventRepository.findById(eventId).orElseThrow();
                        if (e.getCapacity() >= 1) {
                            e.setCapacity(e.getCapacity() - 1);
                            eventRepository.save(e); // Will throw optimistic locking exception if version mismatch
                            
                            Reservation r = Reservation.builder()
                                    .eventId(eventId)
                                    .userId(3L) // Customer from seed data
                                    .status(ReservationStatus.PENDING)
                                    .seats(1)
                                    .build();
                            reservationRepository.save(r);
                            return true;
                        }
                        return false;
                    });
                    // Only increment after the transaction commits successfully
                    if (Boolean.TRUE.equals(reserved)) {
                        successfulReservations.incrementAndGet();
                    } else {
                        failedReservations.incrementAndGet();
                    }

                } catch (ObjectOptimisticLockingFailureException ex) {
                    failedReservations.incrementAndGet();
                } catch (Exception ex) {
                    // Other failures
                    failedReservations.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Starts all threads at the exact same moment
        doneLatch.await(10, TimeUnit.SECONDS);

        // Assertions
        Event finalEvent = eventRepository.findById(eventId).orElseThrow();
        
        // Exact 1 success, 9 failures
        assertEquals(1, successfulReservations.get(), "Only 1 reservation should be successful");
        assertEquals(9, failedReservations.get(), "9 reservations should fail");
        assertEquals(0, finalEvent.getCapacity(), "Event capacity should be 0");
        
        List<Reservation> finalReservations = reservationRepository.findByEventId(eventId);
        assertEquals(1, finalReservations.size(), "Only 1 reservation record should exist");
        
        executorService.shutdown();
    }
}
