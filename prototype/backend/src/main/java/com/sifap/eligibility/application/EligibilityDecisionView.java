package com.sifap.eligibility.application;

import com.sifap.eligibility.domain.EligibilityDecisionStatus;

import java.util.UUID;

public record EligibilityDecisionView(
        UUID beneficiaryId,
        UUID programId,
        EligibilityDecisionStatus decision,
        String reason) {
}