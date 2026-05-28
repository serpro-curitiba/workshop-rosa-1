package com.sifap.beneficiary.domain;

public final class DependentNotAllowedException extends RuntimeException {

    public DependentNotAllowedException(String message) {
        super(message);
    }
}