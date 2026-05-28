package com.sifap.payment.application;

public class PaymentStatusTransitionException extends RuntimeException {

    public PaymentStatusTransitionException(String message) {
        super(message);
    }
}