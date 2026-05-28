package com.sifap.payment.infrastructure;

public class BatchExecutionNotFoundException extends RuntimeException {

    public BatchExecutionNotFoundException(String message) {
        super(message);
    }
}