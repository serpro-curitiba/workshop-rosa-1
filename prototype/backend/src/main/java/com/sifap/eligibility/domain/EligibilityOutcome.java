package com.sifap.eligibility.domain;

public record EligibilityOutcome(EligibilityDecisionStatus decision, String reason) {

    public static EligibilityOutcome approved(String reason) {
        return new EligibilityOutcome(EligibilityDecisionStatus.APPROVED, reason);
    }

    public static EligibilityOutcome rejected(String reason) {
        return new EligibilityOutcome(EligibilityDecisionStatus.REJECTED, reason);
    }
}