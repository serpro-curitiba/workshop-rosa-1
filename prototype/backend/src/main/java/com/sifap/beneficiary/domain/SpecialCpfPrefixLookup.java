package com.sifap.beneficiary.domain;

@FunctionalInterface
public interface SpecialCpfPrefixLookup {

    boolean containsPrefix(String prefix);
}