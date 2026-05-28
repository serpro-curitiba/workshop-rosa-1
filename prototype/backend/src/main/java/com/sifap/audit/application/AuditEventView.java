package com.sifap.audit.application;

import java.time.Instant;
import java.util.UUID;

public record AuditEventView(
        String entityType,
        UUID entityId,
        String action,
        String previousState,
        String newState,
        String actor,
        Instant occurredAt,
        String reason) {
}