package com.sifap.payment.infrastructure;

import com.sifap.payment.application.ReturnFileEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class ReconciliationCsvParser {

    public List<ReturnFileEntry> parse(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            return List.of();
        }

        return csvContent.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.toLowerCase().startsWith("payment_id"))
                .map(this::parseLine)
                .toList();
    }

    private ReturnFileEntry parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid reconciliation file line: " + line);
        }

        return new ReturnFileEntry(UUID.fromString(parts[0].trim()), new BigDecimal(parts[1].trim()));
    }
}