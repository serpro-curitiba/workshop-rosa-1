package com.sifap.payment.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.sifap.payment.application.ReturnFileEntry;

@RestController
@RequestMapping("/api/v1/batch")
@Tag(name = "batch")
public class BatchOperationsController {

    private final JobLauncher jobLauncher;
    private final Job generatePaymentCycleJob;
    private final Job reconciliationJob;
    private final JobExplorer jobExplorer;
    private final ReconciliationUploadStore reconciliationUploadStore;
    private final ReconciliationCsvParser reconciliationCsvParser;

    public BatchOperationsController(
            JobLauncher jobLauncher,
            @Qualifier("generatePaymentCycleJob") Job generatePaymentCycleJob,
            @Qualifier("reconciliationJob") Job reconciliationJob,
            JobExplorer jobExplorer,
            ReconciliationUploadStore reconciliationUploadStore,
            ReconciliationCsvParser reconciliationCsvParser) {
        this.jobLauncher = jobLauncher;
        this.generatePaymentCycleJob = generatePaymentCycleJob;
        this.reconciliationJob = reconciliationJob;
        this.jobExplorer = jobExplorer;
        this.reconciliationUploadStore = reconciliationUploadStore;
        this.reconciliationCsvParser = reconciliationCsvParser;
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get batch job execution status by execution id")
    public BatchExecutionStatusResponse getExecutionStatus(@PathVariable Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new BatchExecutionNotFoundException("Batch execution not found: " + executionId);
        }

        return new BatchExecutionStatusResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getExitStatus().getExitCode(),
                execution.getCreateTime() == null ? null : execution.getCreateTime().toString(),
                execution.getStartTime() == null ? null : execution.getStartTime().toString(),
                execution.getEndTime() == null ? null : execution.getEndTime().toString());
    }

    @GetMapping("/executions")
    @Operation(summary = "List recent batch executions by job name")
    public BatchExecutionListResponse listExecutions(
            @RequestParam String jobName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromCreatedAt,
            @RequestParam(required = false) String toCreatedAt) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        BatchStatus filterStatus = parseFilterStatus(status);
        LocalDateTime createdFrom = parseDateFilter(fromCreatedAt, "fromCreatedAt");
        LocalDateTime createdTo = parseDateFilter(toCreatedAt, "toCreatedAt");
        validateDateRange(createdFrom, createdTo);

        int start = page * size;
        List<BatchExecutionStatusResponse> executions = jobExplorer.getJobInstances(jobName, start, size)
                .stream()
                .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
                .filter(execution -> filterStatus == null || filterStatus.equals(execution.getStatus()))
                .filter(execution -> isWithinCreatedRange(execution, createdFrom, createdTo))
                .sorted(Comparator.comparing(JobExecution::getId).reversed())
                .limit(size)
                .map(this::toExecutionStatusResponse)
                .toList();

        long totalJobInstances = resolveJobInstanceCount(jobName);

        return new BatchExecutionListResponse(jobName, page, size, totalJobInstances, executions.size(), executions);
    }

    @PostMapping("/payment-cycles/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Run payment cycle batch job")
    public BatchJobResponse runPaymentCycle(
            @RequestParam String referenceYearMonth,
            @RequestParam(required = false) String requestId) {
        JobParameters parameters = new JobParametersBuilder()
                .addString("referenceYearMonth", referenceYearMonth)
                .addJobParameters(buildIdempotentParameters(requestId))
                .toJobParameters();
        JobExecution execution = runJob(generatePaymentCycleJob, parameters);
        return new BatchJobResponse(execution.getId(), execution.getStatus().name());
    }

    @PostMapping("/reconciliation/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Run reconciliation batch job")
    public BatchJobResponse runReconciliation(
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String stagingToken) {
        JobExecution execution = runJob(reconciliationJob, buildReconciliationParameters(requestId, stagingToken));
        return new BatchJobResponse(execution.getId(), execution.getStatus().name());
    }

    @PostMapping(path = "/reconciliation/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Upload reconciliation return file for next batch run")
    public ReconciliationUploadResponse uploadReconciliationFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String requestId)
            throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation file cannot be empty");
        }

        List<ReturnFileEntry> entries = reconciliationCsvParser.parse(new String(file.getBytes(), StandardCharsets.UTF_8));
        String resolvedStagingToken = resolveUploadStagingToken(requestId);
        reconciliationUploadStore.stageEntries(resolvedStagingToken, entries);
        if (requestId == null || requestId.isBlank()) {
            // Backward compatibility: upload without requestId remains consumable by plain run endpoint.
            reconciliationUploadStore.stageEntries(ReconciliationUploadStore.DEFAULT_STAGING_TOKEN, entries);
        }
        return new ReconciliationUploadResponse(entries.size(), resolvedStagingToken);
    }

    @PostMapping(path = "/reconciliation/upload-and-run", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Upload reconciliation return file and run reconciliation batch job")
    public ReconciliationRunResponse uploadAndRunReconciliation(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String requestId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation file cannot be empty");
        }

        List<ReturnFileEntry> entries = reconciliationCsvParser.parse(new String(file.getBytes(), StandardCharsets.UTF_8));
        String resolvedStagingToken = resolveUploadStagingToken(requestId);
        reconciliationUploadStore.stageEntries(resolvedStagingToken, entries);

        JobExecution execution = runJob(reconciliationJob, buildReconciliationParameters(requestId, resolvedStagingToken));
        return new ReconciliationRunResponse(entries.size(), resolvedStagingToken, execution.getId(), execution.getStatus().name());
    }

    private JobExecution runJob(Job job, JobParameters parameters) {
        try {
            return jobLauncher.run(job, parameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobRestartException e) {
            throw new BatchRequestConflictException("Batch request is already running or has already completed", e);
        } catch (JobParametersInvalidException e) {
            throw new IllegalArgumentException("Invalid batch request parameters", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to launch batch job", e);
        }
    }

    private JobParameters buildIdempotentParameters(String requestId) {
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        if (requestId != null && !requestId.isBlank()) {
            parametersBuilder.addString("requestId", requestId);
        } else {
            parametersBuilder.addLong("requestedAt", System.currentTimeMillis());
        }
        return parametersBuilder.toJobParameters();
    }

    private JobParameters buildReconciliationParameters(String requestId, String stagingToken) {
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addJobParameters(buildIdempotentParameters(requestId));
        parametersBuilder.addString("stagingToken", resolveRunStagingToken(requestId, stagingToken));
        return parametersBuilder.toJobParameters();
    }

    private String resolveUploadStagingToken(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveRunStagingToken(String requestId, String stagingToken) {
        if (stagingToken != null && !stagingToken.isBlank()) {
            return stagingToken;
        }
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return ReconciliationUploadStore.DEFAULT_STAGING_TOKEN;
    }

    private BatchExecutionStatusResponse toExecutionStatusResponse(JobExecution execution) {
        return new BatchExecutionStatusResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getExitStatus().getExitCode(),
                execution.getCreateTime() == null ? null : execution.getCreateTime().toString(),
                execution.getStartTime() == null ? null : execution.getStartTime().toString(),
                execution.getEndTime() == null ? null : execution.getEndTime().toString());
    }

    private long resolveJobInstanceCount(String jobName) {
        try {
            return jobExplorer.getJobInstanceCount(jobName);
        } catch (NoSuchJobException ignored) {
            return 0;
        }
    }

    private BatchStatus parseFilterStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return BatchStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid status filter: " + status);
        }
    }

    private LocalDateTime parseDateFilter(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid datetime for " + parameterName + ": " + value);
        }
    }

    private void validateDateRange(LocalDateTime createdFrom, LocalDateTime createdTo) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("fromCreatedAt must be before or equal to toCreatedAt");
        }
    }

    private boolean isWithinCreatedRange(JobExecution execution, LocalDateTime createdFrom, LocalDateTime createdTo) {
        LocalDateTime created = execution.getCreateTime();
        if (created == null && (createdFrom != null || createdTo != null)) {
            return false;
        }
        if (createdFrom != null && created.isBefore(createdFrom)) {
            return false;
        }
        if (createdTo != null && created.isAfter(createdTo)) {
            return false;
        }
        return true;
    }

    public record BatchJobResponse(Long executionId, String status) {
    }

    public record BatchExecutionStatusResponse(
            Long executionId,
            String status,
            String exitCode,
            String createTime,
            String startTime,
            String endTime) {
    }

    public record BatchExecutionListResponse(
            String jobName,
            int page,
            int size,
            long totalJobInstances,
            int returnedExecutions,
            List<BatchExecutionStatusResponse> executions) {
    }

    public record ReconciliationUploadResponse(int uploadedEntries, String stagingToken) {
    }

    public record ReconciliationRunResponse(int uploadedEntries, String stagingToken, Long executionId, String status) {
    }
}