package com.sifap.eligibility.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import com.sifap.beneficiary.domain.BeneficiaryStatus;
import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.eligibility.domain.EligibilityDecisionEntity;
import com.sifap.eligibility.domain.EligibilityDecisionStatus;
import com.sifap.eligibility.infrastructure.EligibilityDecisionRepository;
import com.sifap.program.domain.ProgramEntity;
import com.sifap.program.domain.ProgramType;
import com.sifap.program.infrastructure.ProgramRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EligibilityEvaluationServiceTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private EligibilityDecisionRepository eligibilityDecisionRepository;

    private EligibilityEvaluationService eligibilityEvaluationService;

    @BeforeEach
    void setUp() {
        eligibilityEvaluationService = new EligibilityEvaluationService(
                beneficiaryRepository,
                programRepository,
                eligibilityDecisionRepository);
    }

    @Test
    void should_approve_for_region_ninety_nine() {
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(40),
                BeneficiaryStatus.ACTIVE,
                99,
                3,
                new BigDecimal("10000.00"));

        ProgramEntity assistentialProgram = TestProgramFactory.assistential(programId);

        given(beneficiaryRepository.findWithDependentsById(beneficiaryId)).willReturn(Optional.of(beneficiary));
        given(programRepository.findById(programId)).willReturn(Optional.of(assistentialProgram));
        given(eligibilityDecisionRepository.save(any(EligibilityDecisionEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        EligibilityDecisionView view = eligibilityEvaluationService.evaluate(beneficiaryId, programId);

        assertThat(view.decision()).isEqualTo(EligibilityDecisionStatus.APPROVED);
        assertThat(view.reason()).isEqualTo("REGIAO_ESPECIAL_99");
    }

    @Test
    void should_reject_for_assistential_program_when_income_exceeds_limit_without_dependents() {
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(40),
                BeneficiaryStatus.ACTIVE,
                10,
                1,
                new BigDecimal("700.00"));

        ProgramEntity assistentialProgram = TestProgramFactory.assistential(programId);

        given(beneficiaryRepository.findWithDependentsById(beneficiaryId)).willReturn(Optional.of(beneficiary));
        given(programRepository.findById(programId)).willReturn(Optional.of(assistentialProgram));
        given(eligibilityDecisionRepository.save(any(EligibilityDecisionEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        EligibilityDecisionView view = eligibilityEvaluationService.evaluate(beneficiaryId, programId);

        assertThat(view.decision()).isEqualTo(EligibilityDecisionStatus.REJECTED);
        assertThat(view.reason()).isEqualTo("RENDA_ACIMA_LIMITE_ASSISTENCIAL");
    }

    private static final class TestProgramFactory {

        private static ProgramEntity assistential(UUID programId) {
            return ProgramEntity.withIdAndType(programId, ProgramType.ASSISTENCIAL);
        }
    }
}