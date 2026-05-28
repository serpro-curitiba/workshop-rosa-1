package com.sifap.payment.application;

import org.springframework.stereotype.Component;

import java.util.List;

public interface PaymentReconciliationInputProvider {

    List<ReturnFileEntry> loadEntries();

    default List<ReturnFileEntry> loadEntries(String stagingToken) {
        return loadEntries();
    }

    @Component
    class EmptyPaymentReconciliationInputProvider implements PaymentReconciliationInputProvider {

        @Override
        public List<ReturnFileEntry> loadEntries() {
            return List.of();
        }

        @Override
        public List<ReturnFileEntry> loadEntries(String stagingToken) {
            return List.of();
        }
    }
}