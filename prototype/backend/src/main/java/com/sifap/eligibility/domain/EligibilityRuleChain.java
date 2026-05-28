package com.sifap.eligibility.domain;

import java.util.List;
import java.util.Objects;

public class EligibilityRuleChain {

    private final List<EligibilityRule> rules;

    public EligibilityRuleChain(List<EligibilityRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules must not be null"));
    }

    public EligibilityOutcome evaluate(EligibilityContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .findFirst()
                .orElseGet(() -> EligibilityOutcome.approved("APROVADO_SEM_RESTRICOES"));
    }
}