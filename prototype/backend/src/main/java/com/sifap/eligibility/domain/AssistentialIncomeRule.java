package com.sifap.eligibility.domain;

import com.sifap.program.domain.ProgramType;

import java.math.BigDecimal;
import java.util.Optional;

public class AssistentialIncomeRule implements EligibilityRule {

    private static final BigDecimal LIMIT = new BigDecimal("600.00");

    @Override
    public Optional<EligibilityOutcome> evaluate(EligibilityContext context) {
        if (context.programType() == ProgramType.ASSISTENCIAL
                && context.familyIncome().compareTo(LIMIT) > 0
                && context.dependentsCount() == 0) {
            return Optional.of(EligibilityOutcome.rejected("RENDA_ACIMA_LIMITE_ASSISTENCIAL"));
        }
        return Optional.empty();
    }
}