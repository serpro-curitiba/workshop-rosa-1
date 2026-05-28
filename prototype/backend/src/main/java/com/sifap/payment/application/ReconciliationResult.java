package com.sifap.payment.application;

import com.sifap.shared.domain.PaymentStatus;

import java.util.UUID;

public record ReconciliationResult(UUID paymentId, PaymentStatus status, boolean changed) {
}