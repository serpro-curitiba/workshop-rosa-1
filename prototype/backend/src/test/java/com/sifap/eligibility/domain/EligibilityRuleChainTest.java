package com.sifap.eligibility.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.program.domain.ProgramType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EligibilityRuleChainTest {

    private final EligibilityRuleChain chain = new EligibilityRuleChain(List.of(
            new RegionSpecialRule(),
            new AssistentialIncomeRule()));

    @Test
    void should_approve_when_region_is_ninety_nine() {
        EligibilityOutcome outcome = chain.evaluate(new EligibilityContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                99,
                new BigDecimal("10000.00"),
                0,
                ProgramType.ASSISTENCIAL));

        assertThat(outcome.decision()).isEqualTo(EligibilityDecisionStatus.APPROVED);
        assertThat(outcome.reason()).isEqualTo("REGIAO_ESPECIAL_99");
    }

    @Test
    void should_reject_assistential_program_with_income_above_limit_and_no_dependents() {
        EligibilityOutcome outcome = chain.evaluate(new EligibilityContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                new BigDecimal("700.00"),
                0,
                ProgramType.ASSISTENCIAL));

        assertThat(outcome.decision()).isEqualTo(EligibilityDecisionStatus.REJECTED);
        assertThat(outcome.reason()).isEqualTo("RENDA_ACIMA_LIMITE_ASSISTENCIAL");
    }
}