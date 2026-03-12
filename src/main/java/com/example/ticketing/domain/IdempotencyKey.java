package com.example.ticketing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "request_hash")
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime ttl;

    public IdempotencyKey() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getTtl() { return ttl; }
    public void setTtl(LocalDateTime ttl) { this.ttl = ttl; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private IdempotencyKey obj = new IdempotencyKey();
        public Builder id(Long id) { obj.setId(id); return this; }
        public Builder idempotencyKey(String idempotencyKey) { obj.setIdempotencyKey(idempotencyKey); return this; }
        public Builder endpoint(String endpoint) { obj.setEndpoint(endpoint); return this; }
        public Builder requestHash(String requestHash) { obj.setRequestHash(requestHash); return this; }
        public Builder responseBody(String responseBody) { obj.setResponseBody(responseBody); return this; }
        public Builder responseStatus(Integer responseStatus) { obj.setResponseStatus(responseStatus); return this; }
        public Builder status(String status) { obj.setStatus(status); return this; }
        public Builder createdAt(LocalDateTime createdAt) { obj.setCreatedAt(createdAt); return this; }
        public Builder ttl(LocalDateTime ttl) { obj.setTtl(ttl); return this; }
        public IdempotencyKey build() { return obj; }
    }
}
