package com.smartparking.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String action; // CREATE, UPDATE, DELETE, VIEW, LOGIN
    private String entityType; // Slot, Booking, Payment, etc.
    private Long entityId;
    private String changes; // JSON of what changed
    private String ipAddress;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, FAILED

    public AuditLog() {}

    public AuditLog(Long userId, String action, String entityType, Long entityId) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
