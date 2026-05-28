package com.sifap.beneficiary.application;

import com.sifap.beneficiary.domain.BeneficiaryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record CreateBeneficiaryCommand(
        String cpf,
        String name,
        LocalDate birthDate,
        BeneficiaryStatus status,
        int codRegion,
        int familyMembers,
        BigDecimal familyIncome) {

    public CreateBeneficiaryCommand {
        Objects.requireNonNull(cpf, "cpf must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(birthDate, "birthDate must not be null");
        Objects.requireNonNull(familyIncome, "familyIncome must not be null");
    }
}