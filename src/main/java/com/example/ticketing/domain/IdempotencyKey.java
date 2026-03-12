package com.example.ticketing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String endpoint;

    private String requestHash;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private Integer responseStatus;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime ttl;

    protected IdempotencyKey() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getTtl() {
        return ttl;
    }

    public void setTtl(LocalDateTime ttl) {
        this.ttl = ttl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final IdempotencyKey key = new IdempotencyKey();

        public Builder idempotencyKey(String idempotencyKey) {
            key.setIdempotencyKey(idempotencyKey);
            return this;
        }

        public Builder endpoint(String endpoint) {
            key.setEndpoint(endpoint);
            return this;
        }

        public Builder requestHash(String requestHash) {
            key.setRequestHash(requestHash);
            return this;
        }

        public Builder responseBody(String responseBody) {
            key.setResponseBody(responseBody);
            return this;
        }

        public Builder responseStatus(Integer responseStatus) {
            key.setResponseStatus(responseStatus);
            return this;
        }

        public Builder status(String status) {
            key.setStatus(status);
            return this;
        }

        public Builder ttl(LocalDateTime ttl) {
            key.setTtl(ttl);
            return this;
        }

        public IdempotencyKey build() {
            return key;
        }
    }
}
