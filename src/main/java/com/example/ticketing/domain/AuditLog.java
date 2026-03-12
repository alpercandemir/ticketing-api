package com.example.ticketing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "ip")
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AuditLog obj = new AuditLog();
        public Builder id(Long id) { obj.setId(id); return this; }
        public Builder actorId(Long actorId) { obj.setActorId(actorId); return this; }
        public Builder action(String action) { obj.setAction(action); return this; }
        public Builder resourceType(String resourceType) { obj.setResourceType(resourceType); return this; }
        public Builder resourceId(String resourceId) { obj.setResourceId(resourceId); return this; }
        public Builder ip(String ip) { obj.setIp(ip); return this; }
        public Builder userAgent(String userAgent) { obj.setUserAgent(userAgent); return this; }
        public Builder createdAt(LocalDateTime createdAt) { obj.setCreatedAt(createdAt); return this; }
        public AuditLog build() { return obj; }
    }
}
