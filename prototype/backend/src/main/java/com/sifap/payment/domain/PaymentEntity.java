package com.sifap.payment.domain;

import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(name = "uq_payment_beneficiary_month", columnNames = {
                "beneficiary_id", "reference_year_month"
        }))
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "beneficiary_id", nullable = false)
    private UUID beneficiaryId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "reference_year_month", nullable = false, length = 6)
    private String referenceYearMonth;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "corrected", nullable = false)
    private boolean corrected;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PaymentEntity() {
    }

    private PaymentEntity(UUID beneficiaryId, UUID programId, String referenceYearMonth, BigDecimal grossAmount,
            BigDecimal netAmount, PaymentStatus status, boolean corrected) {
        this.beneficiaryId = Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        this.programId = Objects.requireNonNull(programId, "programId must not be null");
        this.referenceYearMonth = Objects.requireNonNull(referenceYearMonth, "referenceYearMonth must not be null");
        this.grossAmount = Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        this.netAmount = Objects.requireNonNull(netAmount, "netAmount must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.corrected = corrected;
    }

    public static PaymentEntity pending(UUID beneficiaryId, UUID programId, String referenceYearMonth, Money grossAmount) {
        return create(beneficiaryId, programId, referenceYearMonth, grossAmount, grossAmount, PaymentStatus.PENDING);
    }

    public static PaymentEntity create(UUID beneficiaryId, UUID programId, String referenceYearMonth, Money grossAmount,
            Money netAmount, PaymentStatus status) {
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        Objects.requireNonNull(netAmount, "netAmount must not be null");
        return new PaymentEntity(
                beneficiaryId,
                programId,
                referenceYearMonth,
                grossAmount.amount(),
                netAmount.amount(),
            status,
            false);
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
    }

    public boolean applyCorrection(Money delta) {
        Objects.requireNonNull(delta, "delta must not be null");
        if (status != PaymentStatus.APPROVED || corrected || !delta.isPositive()) {
            return false;
        }
        netAmount = Money.of(netAmount).add(delta).amount();
        corrected = true;
        return true;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public UUID getProgramId() {
        return programId;
    }

    public String getReferenceYearMonth() {
        return referenceYearMonth;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public boolean isCorrected() {
        return corrected;
    }
}