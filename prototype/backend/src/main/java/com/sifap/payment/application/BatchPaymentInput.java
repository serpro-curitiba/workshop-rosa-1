package com.sifap.payment.application;

import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.ProgramType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BatchPaymentInput(
        UUID beneficiaryId,
        UUID programId,
        String referenceYearMonth,
        BigDecimal baseValue,
        BigDecimal regionalFactor,
        BigDecimal familyFactor,
        BigDecimal incomeFactor,
        BigDecimal ageFactor,
        ProgramType programType,
        List<BatchDiscountInput> discounts) {

    public record BatchDiscountInput(DiscountType type, BigDecimal amount) {
    }
}