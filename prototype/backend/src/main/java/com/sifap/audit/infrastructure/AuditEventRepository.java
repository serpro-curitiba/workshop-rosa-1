package com.sifap.audit.infrastructure;

import com.sifap.audit.domain.AuditEventEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends Repository<AuditEventEntity, UUID> {

    AuditEventEntity save(AuditEventEntity entity);

    List<AuditEventEntity> findAll();

    @Query("""
            select a from AuditEventEntity a
            where (:entityType is null or a.entityType = :entityType)
              and (:entityId is null or a.entityId = :entityId)
              and (:from is null or a.occurredAt >= :from)
              and (:to is null or a.occurredAt <= :to)
            order by a.occurredAt desc
            """)
    List<AuditEventEntity> findByFilters(
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}