package com.example.ticketing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Integer seats;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Reservation() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Reservation reservation = new Reservation();

        public Builder id(Long id) {
            reservation.setId(id);
            return this;
        }

        public Builder eventId(Long eventId) {
            reservation.setEventId(eventId);
            return this;
        }

        public Builder userId(Long userId) {
            reservation.setUserId(userId);
            return this;
        }

        public Builder status(ReservationStatus status) {
            reservation.setStatus(status);
            return this;
        }

        public Builder seats(Integer seats) {
            reservation.setSeats(seats);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            reservation.setCreatedAt(createdAt);
            return this;
        }

        public Reservation build() {
            return reservation;
        }
    }
}
