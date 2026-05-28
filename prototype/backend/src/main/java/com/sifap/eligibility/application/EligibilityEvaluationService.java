package com.sifap.eligibility.application;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import com.sifap.beneficiary.domain.BeneficiaryNotFoundException;
import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.eligibility.domain.AssistentialIncomeRule;
import com.sifap.eligibility.domain.EligibilityContext;
import com.sifap.eligibility.domain.EligibilityDecisionEntity;
import com.sifap.eligibility.domain.EligibilityOutcome;
import com.sifap.eligibility.domain.EligibilityRule;
import com.sifap.eligibility.domain.EligibilityRuleChain;
import com.sifap.eligibility.domain.RegionSpecialRule;
import com.sifap.eligibility.infrastructure.EligibilityDecisionRepository;
import com.sifap.program.domain.ProgramEntity;
import com.sifap.program.domain.ProgramNotFoundException;
import com.sifap.program.infrastructure.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EligibilityEvaluationService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final ProgramRepository programRepository;
    private final EligibilityDecisionRepository eligibilityDecisionRepository;
    private final EligibilityRuleChain eligibilityRuleChain;

    public EligibilityEvaluationService(
            BeneficiaryRepository beneficiaryRepository,
            ProgramRepository programRepository,
            EligibilityDecisionRepository eligibilityDecisionRepository) {
        this(beneficiaryRepository, programRepository, eligibilityDecisionRepository,
                List.of(new RegionSpecialRule(), new AssistentialIncomeRule()));
    }

    EligibilityEvaluationService(
            BeneficiaryRepository beneficiaryRepository,
            ProgramRepository programRepository,
            EligibilityDecisionRepository eligibilityDecisionRepository,
            List<EligibilityRule> rules) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.programRepository = programRepository;
        this.eligibilityDecisionRepository = eligibilityDecisionRepository;
        this.eligibilityRuleChain = new EligibilityRuleChain(rules);
    }

    @Transactional
    public EligibilityDecisionView evaluate(UUID beneficiaryId, UUID programId) {
        BeneficiaryEntity beneficiary = beneficiaryRepository.findWithDependentsById(beneficiaryId)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found"));
        ProgramEntity program = programRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException("Program not found"));

        EligibilityContext context = new EligibilityContext(
                beneficiaryId,
                programId,
                beneficiary.getCodRegion(),
                beneficiary.getFamilyIncome(),
                beneficiary.getDependents().size(),
                program.getType());

        EligibilityOutcome outcome = eligibilityRuleChain.evaluate(context);
        EligibilityDecisionEntity decision = eligibilityDecisionRepository.save(
                EligibilityDecisionEntity.create(beneficiaryId, programId, outcome));

        return new EligibilityDecisionView(
                decision.getBeneficiaryId(),
                decision.getProgramId(),
                decision.getDecision(),
                decision.getReason());
    }
}