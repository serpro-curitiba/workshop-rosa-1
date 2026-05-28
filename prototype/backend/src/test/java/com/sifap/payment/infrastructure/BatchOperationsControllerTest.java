package com.sifap.payment.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.payment.application.ReturnFileEntry;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchOperationsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.sifap.shared.infrastructure.ApiExceptionHandler.class)
class BatchOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean(name = "generatePaymentCycleJob")
    private Job generatePaymentCycleJob;

    @MockBean(name = "reconciliationJob")
    private Job reconciliationJob;

        @MockBean
        private JobExplorer jobExplorer;

    @MockBean
    private ReconciliationUploadStore reconciliationUploadStore;

    @MockBean
    private ReconciliationCsvParser reconciliationCsvParser;

        @Test
        void should_get_batch_execution_status() throws Exception {
                JobExecution execution = new JobExecution(606L);
                execution.setStatus(BatchStatus.COMPLETED);
                execution.setExitStatus(ExitStatus.COMPLETED);
                execution.setCreateTime(LocalDateTime.of(2026, 5, 27, 16, 30));
                execution.setStartTime(LocalDateTime.of(2026, 5, 27, 16, 31));
                execution.setEndTime(LocalDateTime.of(2026, 5, 27, 16, 32));
                given(jobExplorer.getJobExecution(606L)).willReturn(execution);

                mockMvc.perform(get("/api/v1/batch/executions/606"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.executionId").value(606))
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.exitCode").value("COMPLETED"));
        }

        @Test
        void should_return_not_found_for_unknown_batch_execution() throws Exception {
                given(jobExplorer.getJobExecution(999L)).willReturn(null);

                mockMvc.perform(get("/api/v1/batch/executions/999"))
                                .andExpect(status().isNotFound());
        }

            @Test
            void should_list_batch_executions_by_job_name() throws Exception {
                JobInstance firstInstance = new JobInstance(1L, "reconciliationJob");
                JobInstance secondInstance = new JobInstance(2L, "reconciliationJob");

                JobExecution olderExecution = new JobExecution(firstInstance, 700L, new JobParameters());
                olderExecution.setStatus(BatchStatus.FAILED);
                olderExecution.setExitStatus(ExitStatus.FAILED);

                JobExecution newerExecution = new JobExecution(secondInstance, 800L, new JobParameters());
                newerExecution.setStatus(BatchStatus.COMPLETED);
                newerExecution.setExitStatus(ExitStatus.COMPLETED);

                given(jobExplorer.getJobInstances("reconciliationJob", 0, 2))
                        .willReturn(List.of(firstInstance, secondInstance));
                given(jobExplorer.getJobExecutions(firstInstance)).willReturn(List.of(olderExecution));
                given(jobExplorer.getJobExecutions(secondInstance)).willReturn(List.of(newerExecution));
                given(jobExplorer.getJobInstanceCount("reconciliationJob")).willReturn(12L);

                mockMvc.perform(get("/api/v1/batch/executions")
                                .param("jobName", "reconciliationJob")
                                .param("page", "0")
                                .param("size", "2"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.jobName").value("reconciliationJob"))
                        .andExpect(jsonPath("$.totalJobInstances").value(12))
                        .andExpect(jsonPath("$.returnedExecutions").value(2))
                        .andExpect(jsonPath("$.executions[0].executionId").value(800))
                        .andExpect(jsonPath("$.executions[1].executionId").value(700));
            }

            @Test
            void should_filter_batch_execution_list_by_status() throws Exception {
                JobInstance firstInstance = new JobInstance(1L, "reconciliationJob");
                JobInstance secondInstance = new JobInstance(2L, "reconciliationJob");

                JobExecution failedExecution = new JobExecution(firstInstance, 701L, new JobParameters());
                failedExecution.setStatus(BatchStatus.FAILED);
                failedExecution.setExitStatus(ExitStatus.FAILED);

                JobExecution completedExecution = new JobExecution(secondInstance, 801L, new JobParameters());
                completedExecution.setStatus(BatchStatus.COMPLETED);
                completedExecution.setExitStatus(ExitStatus.COMPLETED);

                given(jobExplorer.getJobInstances("reconciliationJob", 0, 10))
                        .willReturn(List.of(firstInstance, secondInstance));
                given(jobExplorer.getJobExecutions(firstInstance)).willReturn(List.of(failedExecution));
                given(jobExplorer.getJobExecutions(secondInstance)).willReturn(List.of(completedExecution));
                given(jobExplorer.getJobInstanceCount("reconciliationJob")).willReturn(12L);

                mockMvc.perform(get("/api/v1/batch/executions")
                                .param("jobName", "reconciliationJob")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "completed"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.returnedExecutions").value(1))
                        .andExpect(jsonPath("$.executions[0].executionId").value(801))
                        .andExpect(jsonPath("$.executions[0].status").value("COMPLETED"));
            }

            @Test
            void should_filter_batch_execution_list_by_created_time_range() throws Exception {
                JobInstance firstInstance = new JobInstance(1L, "reconciliationJob");
                JobInstance secondInstance = new JobInstance(2L, "reconciliationJob");

                JobExecution olderExecution = new JobExecution(firstInstance, 702L, new JobParameters());
                olderExecution.setStatus(BatchStatus.COMPLETED);
                olderExecution.setExitStatus(ExitStatus.COMPLETED);
                olderExecution.setCreateTime(LocalDateTime.of(2026, 5, 26, 8, 0));

                JobExecution inRangeExecution = new JobExecution(secondInstance, 802L, new JobParameters());
                inRangeExecution.setStatus(BatchStatus.COMPLETED);
                inRangeExecution.setExitStatus(ExitStatus.COMPLETED);
                inRangeExecution.setCreateTime(LocalDateTime.of(2026, 5, 27, 10, 0));

                given(jobExplorer.getJobInstances("reconciliationJob", 0, 10))
                        .willReturn(List.of(firstInstance, secondInstance));
                given(jobExplorer.getJobExecutions(firstInstance)).willReturn(List.of(olderExecution));
                given(jobExplorer.getJobExecutions(secondInstance)).willReturn(List.of(inRangeExecution));
                given(jobExplorer.getJobInstanceCount("reconciliationJob")).willReturn(12L);

                mockMvc.perform(get("/api/v1/batch/executions")
                                .param("jobName", "reconciliationJob")
                                .param("page", "0")
                                .param("size", "10")
                                .param("fromCreatedAt", "2026-05-27T00:00:00")
                                .param("toCreatedAt", "2026-05-27T23:59:59"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.returnedExecutions").value(1))
                        .andExpect(jsonPath("$.executions[0].executionId").value(802));
            }

            @Test
            void should_reject_invalid_pagination_for_execution_list() throws Exception {
                mockMvc.perform(get("/api/v1/batch/executions")
                                .param("jobName", "reconciliationJob")
                                .param("page", "-1")
                                .param("size", "0"))
                        .andExpect(status().isBadRequest());
            }

        @Test
        void should_reject_invalid_status_filter_for_execution_list() throws Exception {
                mockMvc.perform(get("/api/v1/batch/executions")
                                                .param("jobName", "reconciliationJob")
                                                .param("status", "done"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void should_reject_invalid_created_time_range_for_execution_list() throws Exception {
                mockMvc.perform(get("/api/v1/batch/executions")
                                                .param("jobName", "reconciliationJob")
                                                .param("fromCreatedAt", "2026-05-28T00:00:00")
                                                .param("toCreatedAt", "2026-05-27T00:00:00"))
                                .andExpect(status().isBadRequest());
        }

    @Test
    void should_trigger_payment_cycle_job() throws Exception {
        JobExecution execution = new JobExecution(101L);
        given(jobLauncher.run(any(Job.class), any())).willReturn(execution);

        mockMvc.perform(post("/api/v1/batch/payment-cycles/run")
                        .param("referenceYearMonth", "202607"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(101));
    }

    @Test
    void should_trigger_reconciliation_job() throws Exception {
        JobExecution execution = new JobExecution(202L);
        given(jobLauncher.run(any(Job.class), any())).willReturn(execution);

        mockMvc.perform(post("/api/v1/batch/reconciliation/run"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(202));

        verify(jobLauncher).run(any(Job.class), argThat(parameters ->
                ReconciliationUploadStore.DEFAULT_STAGING_TOKEN.equals(parameters.getString("stagingToken"))));
    }

    @Test
    void should_trigger_reconciliation_job_with_request_id() throws Exception {
        JobExecution execution = new JobExecution(212L);
        given(jobLauncher.run(any(Job.class), any())).willReturn(execution);

        mockMvc.perform(post("/api/v1/batch/reconciliation/run")
                        .param("requestId", "req-212"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(212));

        verify(jobLauncher).run(any(Job.class), argThat(parameters ->
                "req-212".equals(parameters.getString("requestId"))
                        && "req-212".equals(parameters.getString("stagingToken"))
                        && parameters.getParameters().get("requestedAt") == null));
    }

    @Test
    void should_upload_reconciliation_file() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(reconciliationCsvParser.parse(anyString()))
                .willReturn(List.of(new ReturnFileEntry(paymentId, new BigDecimal("99.99"))));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "reconciliation.csv",
                "text/csv",
                ("payment_id,bank_amount\n" + paymentId + ",99.99\n").getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.uploadedEntries").value(1))
                .andExpect(jsonPath("$.stagingToken").isNotEmpty());

        verify(reconciliationUploadStore, times(2)).stageEntries(anyString(), any());
    }

    @Test
    void should_upload_reconciliation_file_with_request_id_as_staging_token() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(reconciliationCsvParser.parse(anyString()))
                .willReturn(List.of(new ReturnFileEntry(paymentId, new BigDecimal("91.11"))));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "reconciliation.csv",
                "text/csv",
                ("payment_id,bank_amount\n" + paymentId + ",91.11\n").getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload")
                        .file(file)
                        .param("requestId", "req-upload"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.uploadedEntries").value(1))
                .andExpect(jsonPath("$.stagingToken").value("req-upload"));

        verify(reconciliationUploadStore).stageEntries(argThat("req-upload"::equals), any());
    }

    @Test
    void should_upload_and_run_reconciliation_job() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(reconciliationCsvParser.parse(anyString()))
                .willReturn(List.of(new ReturnFileEntry(paymentId, new BigDecimal("88.77"))));
        given(jobLauncher.run(any(Job.class), any())).willReturn(new JobExecution(303L));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "reconciliation.csv",
                "text/csv",
                ("payment_id,bank_amount\n" + paymentId + ",88.77\n").getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload-and-run").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.uploadedEntries").value(1))
                .andExpect(jsonPath("$.stagingToken").isNotEmpty())
                .andExpect(jsonPath("$.executionId").value(303));

        verify(reconciliationUploadStore).stageEntries(anyString(), any());
        verify(jobLauncher).run(any(Job.class), any());
    }

    @Test
    void should_upload_and_run_reconciliation_job_with_request_id() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(reconciliationCsvParser.parse(anyString()))
                .willReturn(List.of(new ReturnFileEntry(paymentId, new BigDecimal("77.66"))));
        given(jobLauncher.run(any(Job.class), any())).willReturn(new JobExecution(404L));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "reconciliation.csv",
                "text/csv",
                ("payment_id,bank_amount\n" + paymentId + ",77.66\n").getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload-and-run")
                        .file(file)
                        .param("requestId", "req-404"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.uploadedEntries").value(1))
                .andExpect(jsonPath("$.stagingToken").value("req-404"))
                .andExpect(jsonPath("$.executionId").value(404));

        verify(jobLauncher).run(any(Job.class), argThat(parameters ->
                "req-404".equals(parameters.getString("requestId"))
                        && "req-404".equals(parameters.getString("stagingToken"))
                        && parameters.getParameters().get("requestedAt") == null));
    }

    @Test
    void should_trigger_reconciliation_job_with_explicit_staging_token() throws Exception {
        JobExecution execution = new JobExecution(909L);
        given(jobLauncher.run(any(Job.class), any())).willReturn(execution);

        mockMvc.perform(post("/api/v1/batch/reconciliation/run")
                        .param("stagingToken", "staging-909"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(909));

        verify(jobLauncher).run(any(Job.class), argThat(parameters ->
                "staging-909".equals(parameters.getString("stagingToken"))));
    }

    @Test
    void should_return_conflict_when_request_id_is_reused() throws Exception {
        doThrow(new JobInstanceAlreadyCompleteException("Already completed"))
                .when(jobLauncher)
                .run(any(Job.class), any(JobParameters.class));

        mockMvc.perform(post("/api/v1/batch/reconciliation/run")
                        .param("requestId", "req-duplicated"))
                .andExpect(status().isConflict());
    }

    @Test
    void should_reject_empty_reconciliation_file_upload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "reconciliation.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_reject_empty_reconciliation_file_upload_and_run() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "reconciliation.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/batch/reconciliation/upload-and-run").file(file))
                .andExpect(status().isBadRequest());
    }
}