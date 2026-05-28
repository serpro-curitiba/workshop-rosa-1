package com.sifap.payment.application;

import org.springframework.stereotype.Component;

import java.util.List;

public interface PaymentBatchInputProvider {

    List<BatchPaymentInput> loadForReferenceMonth(String referenceYearMonth);

    @Component
    class EmptyPaymentBatchInputProvider implements PaymentBatchInputProvider {

        @Override
        public List<BatchPaymentInput> loadForReferenceMonth(String referenceYearMonth) {
            return List.of();
        }
    }
}