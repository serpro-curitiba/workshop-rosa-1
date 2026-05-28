package com.sifap.beneficiary.application;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import com.sifap.beneficiary.domain.BeneficiaryNotFoundException;
import com.sifap.beneficiary.domain.DependentEntity;
import com.sifap.beneficiary.domain.DocumentValidationPolicy;
import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.beneficiary.infrastructure.SpecialCpfPrefixRepository;
import com.sifap.payment.application.DuplicatePaymentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final DocumentValidationPolicy documentValidationPolicy;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository, SpecialCpfPrefixRepository specialCpfPrefixRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.documentValidationPolicy = new DocumentValidationPolicy(specialCpfPrefixRepository::existsById);
    }

    @Transactional
    public BeneficiaryView create(CreateBeneficiaryCommand command) {
        String normalizedCpf = documentValidationPolicy.validateAndNormalizeCpf(command.cpf());
        if (beneficiaryRepository.existsByCpf(normalizedCpf)) {
            throw new DuplicatePaymentException("Beneficiary already exists for CPF");
        }

        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                normalizedCpf,
                command.name(),
                command.birthDate(),
                command.status(),
                command.codRegion(),
                command.familyMembers(),
                command.familyIncome());
        beneficiary.applyAutomaticSuspensionIfNeeded();

        return toView(beneficiaryRepository.save(beneficiary));
    }

    @Transactional(readOnly = true)
    public BeneficiaryView getById(UUID beneficiaryId) {
        return beneficiaryRepository.findWithDependentsById(beneficiaryId)
                .map(this::toView)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found"));
    }

    @Transactional
    public BeneficiaryView addDependent(UUID beneficiaryId, AddDependentCommand command) {
        BeneficiaryEntity beneficiary = beneficiaryRepository.findWithDependentsById(beneficiaryId)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found"));

        beneficiary.addDependent(DependentEntity.create(command.name(), command.birthDate(), command.relationship()));
        return toView(beneficiaryRepository.save(beneficiary));
    }

    private BeneficiaryView toView(BeneficiaryEntity entity) {
        List<BeneficiaryView.DependentView> dependents = entity.getDependents().stream()
                .map(dependent -> new BeneficiaryView.DependentView(
                        dependent.getId(),
                        dependent.getName(),
                        dependent.getBirthDate(),
                        dependent.getRelationship()))
                .toList();

        return new BeneficiaryView(
                entity.getId(),
                entity.getCpf(),
                entity.getName(),
                entity.getBirthDate(),
                entity.getStatus(),
                entity.getCodRegion(),
                entity.getFamilyMembers(),
                entity.getFamilyIncome(),
                dependents);
    }
}