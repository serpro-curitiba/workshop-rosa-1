package com.sifap.eligibility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eligibility_decision")
public class EligibilityDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "beneficiary_id", nullable = false)
    private UUID beneficiaryId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private EligibilityDecisionStatus decision;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected EligibilityDecisionEntity() {
    }

    private EligibilityDecisionEntity(UUID beneficiaryId, UUID programId, EligibilityDecisionStatus decision, String reason) {
        this.beneficiaryId = beneficiaryId;
        this.programId = programId;
        this.decision = decision;
        this.reason = reason;
    }

    public static EligibilityDecisionEntity create(UUID beneficiaryId, UUID programId, EligibilityOutcome outcome) {
        return new EligibilityDecisionEntity(beneficiaryId, programId, outcome.decision(), outcome.reason());
    }

    @PrePersist
    void prePersist() {
        decidedAt = Instant.now();
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public UUID getProgramId() {
        return programId;
    }

    public EligibilityDecisionStatus getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }
}