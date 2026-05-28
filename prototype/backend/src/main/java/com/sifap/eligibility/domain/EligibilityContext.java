package com.sifap.eligibility.domain;

import com.sifap.program.domain.ProgramType;

import java.math.BigDecimal;
import java.util.UUID;

public record EligibilityContext(
        UUID beneficiaryId,
        UUID programId,
        int codRegion,
        BigDecimal familyIncome,
        int dependentsCount,
        ProgramType programType) {
}