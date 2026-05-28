package com.sifap.beneficiary.application;

import java.time.LocalDate;
import java.util.Objects;

public record AddDependentCommand(String name, LocalDate birthDate, String relationship) {

    public AddDependentCommand {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(birthDate, "birthDate must not be null");
        Objects.requireNonNull(relationship, "relationship must not be null");
    }
}