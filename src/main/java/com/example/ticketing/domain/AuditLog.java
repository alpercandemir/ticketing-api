package com.example.ticketing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorId;

    @Column(nullable = false)
    private String action;

    private String resourceType;

    private String resourceId;

    private String ip;

    @Column(length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

        private final AuditLog auditLog = new AuditLog();

        public Builder actorId(Long actorId) {
            auditLog.setActorId(actorId);
            return this;
        }

        public Builder action(String action) {
            auditLog.setAction(action);
            return this;
        }

        public Builder resourceType(String resourceType) {
            auditLog.setResourceType(resourceType);
            return this;
        }

        public Builder resourceId(String resourceId) {
            auditLog.setResourceId(resourceId);
            return this;
        }

        public Builder ip(String ip) {
            auditLog.setIp(ip);
            return this;
        }

        public Builder userAgent(String userAgent) {
            auditLog.setUserAgent(userAgent);
            return this;
        }

        public AuditLog build() {
            return auditLog;
        }
    }
}
