package com.sifap.shared.audit;

import java.util.UUID;

public record AuditApplicationEvent(
        String entityType,
        UUID entityId,
        String action,
        String previousState,
        String newState,
        String actor,
        String reason) {
}