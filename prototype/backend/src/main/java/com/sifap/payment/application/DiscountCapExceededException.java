package com.sifap.payment.application;

public final class DiscountCapExceededException extends RuntimeException {

    public DiscountCapExceededException(String message) {
        super(message);
    }
}