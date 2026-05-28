package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.application.EligibilityDecisionView;
import com.sifap.eligibility.application.EligibilityEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eligibility")
@Tag(name = "eligibility")
public class EligibilityController {

    private final EligibilityEvaluationService eligibilityEvaluationService;

    public EligibilityController(EligibilityEvaluationService eligibilityEvaluationService) {
        this.eligibilityEvaluationService = eligibilityEvaluationService;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate beneficiary eligibility for a program")
    public EligibilityDecisionView evaluate(@Valid @RequestBody EvaluateEligibilityRequest request) {
        return eligibilityEvaluationService.evaluate(request.beneficiaryId(), request.programId());
    }

    public record EvaluateEligibilityRequest(@NotNull UUID beneficiaryId, @NotNull UUID programId) {
    }
}