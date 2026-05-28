package com.sifap.beneficiary.domain;

public final class InvalidCpfException extends RuntimeException {

    public InvalidCpfException(String message) {
        super(message);
    }
}