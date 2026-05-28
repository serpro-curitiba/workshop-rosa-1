package com.sifap.payment.infrastructure;

import com.sifap.payment.application.ReturnFileEntry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReconciliationUploadStore {

    public static final String DEFAULT_STAGING_TOKEN = "default";

    private final Map<String, List<ReturnFileEntry>> stagedEntriesByToken = new ConcurrentHashMap<>();

    public void stageEntries(List<ReturnFileEntry> entries) {
        stageEntries(DEFAULT_STAGING_TOKEN, entries);
    }

    public List<ReturnFileEntry> consumeStagedEntries() {
        return consumeStagedEntries(DEFAULT_STAGING_TOKEN);
    }

    public void stageEntries(String stagingToken, List<ReturnFileEntry> entries) {
        stagedEntriesByToken.put(resolveStagingToken(stagingToken), List.copyOf(entries));
    }

    public List<ReturnFileEntry> consumeStagedEntries(String stagingToken) {
        List<ReturnFileEntry> entries = stagedEntriesByToken.remove(resolveStagingToken(stagingToken));
        return entries == null ? List.of() : entries;
    }

    private String resolveStagingToken(String stagingToken) {
        if (stagingToken == null || stagingToken.isBlank()) {
            return DEFAULT_STAGING_TOKEN;
        }
        return stagingToken;
    }
}