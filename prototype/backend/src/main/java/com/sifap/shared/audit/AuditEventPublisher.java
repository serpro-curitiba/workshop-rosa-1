package com.sifap.shared.audit;

import java.util.UUID;

public interface AuditEventPublisher {

    void publish(String entityType, UUID entityId, String action, String previousState,
            String newState, String actor, String reason);
}