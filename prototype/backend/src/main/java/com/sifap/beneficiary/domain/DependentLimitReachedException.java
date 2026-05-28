package com.sifap.beneficiary.domain;

public final class DependentLimitReachedException extends RuntimeException {

    public DependentLimitReachedException(String message) {
        super(message);
    }
}