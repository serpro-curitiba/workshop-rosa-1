package com.sifap.payment.infrastructure;

public class BatchRequestConflictException extends RuntimeException {

    public BatchRequestConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}