package com.sifap.audit.infrastructure;

import com.sifap.audit.application.AuditService;
import com.sifap.shared.audit.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onAuditApplicationEvent(AuditApplicationEvent event) {
        auditService.record(
                event.entityType(),
                event.entityId(),
                event.action(),
                event.previousState(),
                event.newState(),
                event.actor(),
                event.reason());
    }
}