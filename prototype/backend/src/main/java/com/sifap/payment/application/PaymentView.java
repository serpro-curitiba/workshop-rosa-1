package com.sifap.payment.application;

import com.sifap.shared.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentView(
        UUID id,
        UUID beneficiaryId,
        UUID programId,
        String referenceYearMonth,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        PaymentStatus status) {
}