package com.sifap.eligibility.domain;

import java.util.Optional;

@FunctionalInterface
public interface EligibilityRule {

    Optional<EligibilityOutcome> evaluate(EligibilityContext context);
}