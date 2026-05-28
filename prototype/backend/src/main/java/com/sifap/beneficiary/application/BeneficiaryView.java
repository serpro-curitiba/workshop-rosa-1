package com.sifap.beneficiary.application;

import com.sifap.beneficiary.domain.BeneficiaryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BeneficiaryView(
        UUID id,
        String cpf,
        String name,
        LocalDate birthDate,
        BeneficiaryStatus status,
        int codRegion,
        int familyMembers,
        BigDecimal familyIncome,
        List<DependentView> dependents) {

    public record DependentView(UUID id, String name, LocalDate birthDate, String relationship) {
    }
}