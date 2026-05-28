package com.sifap.program.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "program")
public class ProgramEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProgramType type;

    @Column(name = "base_value", nullable = false)
    private BigDecimal baseValue;

    @Column(name = "adjustment_factor", nullable = false)
    private BigDecimal adjustmentFactor;

    @Column(name = "fator_k", nullable = false)
    private BigDecimal fatorK;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProgramEntity() {
    }

    public static ProgramEntity withIdAndType(UUID id, ProgramType type) {
        ProgramEntity entity = new ProgramEntity();
        entity.id = id;
        entity.type = type;
        entity.name = "Program";
        entity.baseValue = BigDecimal.ZERO;
        entity.adjustmentFactor = BigDecimal.ONE;
        entity.fatorK = new BigDecimal("0.347215");
        entity.status = "ACTIVE";
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProgramType getType() {
        return type;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public BigDecimal getAdjustmentFactor() {
        return adjustmentFactor;
    }

    public BigDecimal getFatorK() {
        return fatorK;
    }

    public String getStatus() {
        return status;
    }

    public void updateFatorK(BigDecimal fatorK) {
        this.fatorK = fatorK;
        this.updatedAt = Instant.now();
    }
}