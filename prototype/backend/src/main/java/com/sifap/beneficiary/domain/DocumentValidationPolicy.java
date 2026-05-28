package com.sifap.beneficiary.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public class DocumentValidationPolicy {

    private static final Pattern NON_DIGITS = Pattern.compile("\\D");

    private final SpecialCpfPrefixLookup specialCpfPrefixLookup;

    public DocumentValidationPolicy(SpecialCpfPrefixLookup specialCpfPrefixLookup) {
        this.specialCpfPrefixLookup = Objects.requireNonNull(specialCpfPrefixLookup, "specialCpfPrefixLookup must not be null");
    }

    public String validateAndNormalizeCpf(String cpf) {
        String normalizedCpf = normalize(cpf);
        if (normalizedCpf.isBlank()) {
            throw new InvalidCpfException("CPF is required");
        }
        if (normalizedCpf.length() != 11) {
            throw new InvalidCpfException("CPF must contain 11 digits");
        }
        if (allDigitsEqual(normalizedCpf)) {
            throw new InvalidCpfException("CPF with all repeated digits is not allowed");
        }
        if (specialCpfPrefixLookup.containsPrefix(normalizedCpf.substring(0, 3))) {
            return normalizedCpf;
        }
        if (!hasValidCheckDigits(normalizedCpf)) {
            throw new InvalidCpfException("CPF check digits are invalid");
        }
        return normalizedCpf;
    }

    private String normalize(String cpf) {
        return cpf == null ? "" : NON_DIGITS.matcher(cpf).replaceAll("");
    }

    private boolean allDigitsEqual(String cpf) {
        char first = cpf.charAt(0);
        for (int index = 1; index < cpf.length(); index++) {
            if (cpf.charAt(index) != first) {
                return false;
            }
        }
        return true;
    }

    private boolean hasValidCheckDigits(String cpf) {
        return calculateDigit(cpf, 9, 10) == Character.getNumericValue(cpf.charAt(9))
                && calculateDigit(cpf, 10, 11) == Character.getNumericValue(cpf.charAt(10));
    }

    private int calculateDigit(String cpf, int length, int weightStart) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(cpf.charAt(index)) * (weightStart - index);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}