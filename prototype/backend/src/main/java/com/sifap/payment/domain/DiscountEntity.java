package com.sifap.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_discount")
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DiscountType type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected DiscountEntity() {
    }

    private DiscountEntity(UUID paymentId, DiscountType type, BigDecimal amount) {
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
    }

    public static DiscountEntity create(UUID paymentId, DiscountType type, BigDecimal amount) {
        return new DiscountEntity(paymentId, type, amount);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public DiscountType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}