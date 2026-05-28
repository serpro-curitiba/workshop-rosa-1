package com.sifap.eligibility.domain;

import java.util.Optional;

public class RegionSpecialRule implements EligibilityRule {

    @Override
    public Optional<EligibilityOutcome> evaluate(EligibilityContext context) {
        if (context.codRegion() == 99) {
            return Optional.of(EligibilityOutcome.approved("REGIAO_ESPECIAL_99"));
        }
        return Optional.empty();
    }
}