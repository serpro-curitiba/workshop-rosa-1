package com.sifap.payment.application;

import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.ProgramType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreatePaymentCycleCommand(
        UUID beneficiaryId,
        UUID programId,
        String referenceYearMonth,
        BigDecimal baseValue,
        BigDecimal regionalFactor,
        BigDecimal familyFactor,
        BigDecimal incomeFactor,
        BigDecimal ageFactor,
        ProgramType programType,
        List<CreatePaymentDiscountCommand> discounts) {

    public CreatePaymentCycleCommand {
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(programId, "programId must not be null");
        Objects.requireNonNull(referenceYearMonth, "referenceYearMonth must not be null");
        Objects.requireNonNull(baseValue, "baseValue must not be null");
        Objects.requireNonNull(regionalFactor, "regionalFactor must not be null");
        Objects.requireNonNull(familyFactor, "familyFactor must not be null");
        Objects.requireNonNull(incomeFactor, "incomeFactor must not be null");
        Objects.requireNonNull(ageFactor, "ageFactor must not be null");
        Objects.requireNonNull(programType, "programType must not be null");
        discounts = discounts == null ? List.of() : List.copyOf(discounts);
    }

    public record CreatePaymentDiscountCommand(DiscountType type, BigDecimal amount) {

        public CreatePaymentDiscountCommand {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(amount, "amount must not be null");
        }
    }
}