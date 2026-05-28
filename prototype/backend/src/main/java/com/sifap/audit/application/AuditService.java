package com.sifap.audit.application;

import com.sifap.audit.domain.AuditEventEntity;
import com.sifap.audit.infrastructure.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public AuditEventView record(String entityType, UUID entityId, String action, String previousState,
            String newState, String actor, String reason) {
        AuditEventEntity entity = AuditEventEntity.create(
                entityType,
                entityId,
                action,
                previousState,
                newState,
                actor,
                reason);
        return toView(auditEventRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<AuditEventView> list(String entityType, UUID entityId, Instant from, Instant to) {
        return auditEventRepository.findByFilters(entityType, entityId, from, to)
                .stream()
                .map(this::toView)
                .toList();
    }

    private AuditEventView toView(AuditEventEntity entity) {
        return new AuditEventView(
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getPreviousState(),
                entity.getNewState(),
                entity.getActor(),
                entity.getOccurredAt(),
                entity.getReason());
    }
}