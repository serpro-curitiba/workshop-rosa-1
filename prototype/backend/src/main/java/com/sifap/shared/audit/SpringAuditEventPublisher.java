package com.sifap.shared.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringAuditEventPublisher implements AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringAuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(String entityType, UUID entityId, String action, String previousState,
            String newState, String actor, String reason) {
        applicationEventPublisher.publishEvent(new AuditApplicationEvent(
                entityType,
                entityId,
                action,
                previousState,
                newState,
                actor,
                reason));
    }
}