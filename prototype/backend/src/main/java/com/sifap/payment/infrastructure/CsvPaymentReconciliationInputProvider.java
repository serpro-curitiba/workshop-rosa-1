package com.sifap.payment.infrastructure;

import com.sifap.payment.application.PaymentReconciliationInputProvider;
import com.sifap.payment.application.ReturnFileEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@Primary
public class CsvPaymentReconciliationInputProvider implements PaymentReconciliationInputProvider {

    private final String reconciliationFilePath;
    private final ReconciliationUploadStore reconciliationUploadStore;
    private final ReconciliationCsvParser reconciliationCsvParser;

    public CsvPaymentReconciliationInputProvider(
            @Value("${sifap.batch.reconciliation.file-path:}") String reconciliationFilePath,
            ReconciliationUploadStore reconciliationUploadStore,
            ReconciliationCsvParser reconciliationCsvParser) {
        this.reconciliationFilePath = reconciliationFilePath;
        this.reconciliationUploadStore = reconciliationUploadStore;
        this.reconciliationCsvParser = reconciliationCsvParser;
    }

    @Override
    public List<ReturnFileEntry> loadEntries() {
        return loadEntries(ReconciliationUploadStore.DEFAULT_STAGING_TOKEN);
    }

    @Override
    public List<ReturnFileEntry> loadEntries(String stagingToken) {
        List<ReturnFileEntry> stagedEntries = reconciliationUploadStore.consumeStagedEntries(stagingToken);
        if (!stagedEntries.isEmpty()) {
            return stagedEntries;
        }

        if (reconciliationFilePath == null || reconciliationFilePath.isBlank()) {
            return List.of();
        }

        Path file = Path.of(reconciliationFilePath);
        if (!Files.exists(file)) {
            return List.of();
        }

        try {
            return reconciliationCsvParser.parse(Files.readString(file));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read reconciliation file", exception);
        }
    }
}