package com.sifap.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.payment.application.ReturnFileEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CsvPaymentReconciliationInputProviderTest {

    @Test
    void should_read_entries_from_csv_file() throws Exception {
        UUID paymentId = UUID.randomUUID();
        Path csv = Files.createTempFile("reconciliation", ".csv");
        Files.writeString(csv, "payment_id,bank_amount\n" + paymentId + ",99.98\n");

        CsvPaymentReconciliationInputProvider provider = new CsvPaymentReconciliationInputProvider(
                csv.toString(),
                new ReconciliationUploadStore(),
                new ReconciliationCsvParser());
        List<ReturnFileEntry> entries = provider.loadEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).paymentId()).isEqualTo(paymentId);
        assertThat(entries.get(0).bankAmount()).isEqualByComparingTo("99.98");
    }

    @Test
    void should_prioritize_uploaded_entries_before_file_and_consume_them() {
        ReconciliationUploadStore uploadStore = new ReconciliationUploadStore();
        CsvPaymentReconciliationInputProvider provider = new CsvPaymentReconciliationInputProvider(
                "",
                uploadStore,
                new ReconciliationCsvParser());

        UUID uploadedPaymentId = UUID.randomUUID();
        uploadStore.stageEntries("token-11", List.of(new ReturnFileEntry(uploadedPaymentId, new BigDecimal("11.22"))));

        List<ReturnFileEntry> firstLoad = provider.loadEntries("token-11");
        List<ReturnFileEntry> secondLoad = provider.loadEntries("token-11");

        assertThat(firstLoad).hasSize(1);
        assertThat(firstLoad.get(0).paymentId()).isEqualTo(uploadedPaymentId);
        assertThat(secondLoad).isEmpty();
    }
}