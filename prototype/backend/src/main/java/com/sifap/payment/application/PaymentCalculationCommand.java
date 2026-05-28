package com.sifap.payment.application;

import com.sifap.payment.domain.ProgramType;
import com.sifap.shared.domain.Money;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public record PaymentCalculationCommand(
        Money baseValue,
        BigDecimal regionalFactor,
        BigDecimal familyFactor,
        BigDecimal incomeFactor,
        BigDecimal ageFactor,
        YearMonth referenceMonth,
        ProgramType programType) {

    public PaymentCalculationCommand {
        Objects.requireNonNull(baseValue, "baseValue must not be null");
        Objects.requireNonNull(regionalFactor, "regionalFactor must not be null");
        Objects.requireNonNull(familyFactor, "familyFactor must not be null");
        Objects.requireNonNull(incomeFactor, "incomeFactor must not be null");
        Objects.requireNonNull(ageFactor, "ageFactor must not be null");
        Objects.requireNonNull(referenceMonth, "referenceMonth must not be null");
        Objects.requireNonNull(programType, "programType must not be null");
    }
}