package com.example.ticketing.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean published = false;

    @Version
    @Column(nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Event event = new Event();

        public Builder id(Long id) {
            event.setId(id);
            return this;
        }

        public Builder ownerId(Long ownerId) {
            event.setOwnerId(ownerId);
            return this;
        }

        public Builder title(String title) {
            event.setTitle(title);
            return this;
        }

        public Builder venue(String venue) {
            event.setVenue(venue);
            return this;
        }

        public Builder startsAt(LocalDateTime startsAt) {
            event.setStartsAt(startsAt);
            return this;
        }

        public Builder endsAt(LocalDateTime endsAt) {
            event.setEndsAt(endsAt);
            return this;
        }

        public Builder capacity(Integer capacity) {
            event.setCapacity(capacity);
            return this;
        }

        public Builder published(Boolean published) {
            event.setPublished(published);
            return this;
        }

        public Builder version(Long version) {
            event.setVersion(version);
            return this;
        }

        public Event build() {
            return event;
        }
    }
}
