package com.sifap.beneficiary.infrastructure;

import com.sifap.beneficiary.application.AddDependentCommand;
import com.sifap.beneficiary.application.BeneficiaryService;
import com.sifap.beneficiary.application.BeneficiaryView;
import com.sifap.beneficiary.application.CreateBeneficiaryCommand;
import com.sifap.beneficiary.domain.BeneficiaryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "beneficiary")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a beneficiary")
    public BeneficiaryView create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        return beneficiaryService.create(request.toCommand());
    }

    @GetMapping("/{beneficiaryId}")
    @Operation(summary = "Fetch a beneficiary by id")
    public BeneficiaryView getById(@PathVariable UUID beneficiaryId) {
        return beneficiaryService.getById(beneficiaryId);
    }

    @PostMapping("/{beneficiaryId}/dependents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a dependent to a beneficiary")
    public BeneficiaryView addDependent(@PathVariable UUID beneficiaryId, @Valid @RequestBody AddDependentRequest request) {
        return beneficiaryService.addDependent(beneficiaryId, request.toCommand());
    }

    public record CreateBeneficiaryRequest(
            @NotBlank String cpf,
            @NotBlank String name,
            @NotNull LocalDate birthDate,
            BeneficiaryStatus status,
            int codRegion,
            int familyMembers,
            @NotNull @DecimalMin("0.00") BigDecimal familyIncome) {

        CreateBeneficiaryCommand toCommand() {
            return new CreateBeneficiaryCommand(cpf, name, birthDate, status, codRegion, familyMembers, familyIncome);
        }
    }

    public record AddDependentRequest(
            @NotBlank String name,
            @NotNull LocalDate birthDate,
            @NotBlank String relationship) {

        AddDependentCommand toCommand() {
            return new AddDependentCommand(name, birthDate, relationship);
        }
    }
}