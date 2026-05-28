package com.sifap.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Lob
    @Column(name = "previous_state")
    private String previousState;

    @Lob
    @Column(name = "new_state")
    private String newState;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Lob
    @Column(name = "reason")
    private String reason;

    protected AuditEventEntity() {
    }

    private AuditEventEntity(String entityType, UUID entityId, String action, String previousState, String newState,
            String actor, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.previousState = previousState;
        this.newState = newState;
        this.actor = actor;
        this.reason = reason;
    }

    public static AuditEventEntity create(String entityType, UUID entityId, String action, String previousState,
            String newState, String actor, String reason) {
        return new AuditEventEntity(entityType, entityId, action, previousState, newState, actor, reason);
    }

    @PrePersist
    void prePersist() {
        occurredAt = Instant.now();
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public String getPreviousState() {
        return previousState;
    }

    public String getNewState() {
        return newState;
    }

    public String getActor() {
        return actor;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getReason() {
        return reason;
    }
}