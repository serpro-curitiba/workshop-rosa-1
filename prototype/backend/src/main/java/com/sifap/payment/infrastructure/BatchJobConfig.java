package com.sifap.payment.infrastructure;

import com.sifap.payment.application.BatchPaymentInput;
import com.sifap.payment.application.PaymentBatchInputProvider;
import com.sifap.payment.application.PaymentBatchService;
import com.sifap.payment.application.PaymentReconciliationInputProvider;
import com.sifap.payment.application.ReconciliationService;
import com.sifap.payment.application.ReturnFileEntry;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class BatchJobConfig {

    @Bean
    public Job generatePaymentCycleJob(JobRepository jobRepository, Step generatePaymentCycleStep) {
        return new JobBuilder("generatePaymentCycleJob", jobRepository)
                .start(generatePaymentCycleStep)
                .build();
    }

    @Bean
    public Step generatePaymentCycleStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<BatchPaymentInput> paymentCycleItemReader,
            ItemProcessor<BatchPaymentInput, com.sifap.payment.domain.PaymentEntity> paymentCycleItemProcessor,
            ItemWriter<com.sifap.payment.domain.PaymentEntity> paymentCycleItemWriter) {
        return new StepBuilder("generatePaymentCycleStep", jobRepository)
                .<BatchPaymentInput, com.sifap.payment.domain.PaymentEntity>chunk(50, transactionManager)
                .reader(paymentCycleItemReader)
                .processor(paymentCycleItemProcessor)
                .writer(paymentCycleItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<BatchPaymentInput> paymentCycleItemReader(
            PaymentBatchInputProvider paymentBatchInputProvider,
            @Value("#{jobParameters['referenceYearMonth']}") String referenceYearMonth) {
        String month = referenceYearMonth == null ? "197001" : referenceYearMonth;
        return new ListItemReader<>(paymentBatchInputProvider.loadForReferenceMonth(month));
    }

    @Bean
    public ItemProcessor<BatchPaymentInput, com.sifap.payment.domain.PaymentEntity> paymentCycleItemProcessor(
            PaymentBatchService paymentBatchService) {
        return item -> paymentBatchService.processItem(item).orElse(null);
    }

    @Bean
    public ItemWriter<com.sifap.payment.domain.PaymentEntity> paymentCycleItemWriter(PaymentRepository paymentRepository) {
        return items -> {
            // Items are persisted by PaymentBatchService.processItem; writer is intentionally no-op.
        };
    }

    @Bean
    public Job reconciliationJob(JobRepository jobRepository, Step reconciliationStep) {
        return new JobBuilder("reconciliationJob", jobRepository)
                .start(reconciliationStep)
                .build();
    }

    @Bean
    public Step reconciliationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<ReturnFileEntry> reconciliationItemReader,
            ItemWriter<ReturnFileEntry> reconciliationItemWriter) {
        return new StepBuilder("reconciliationStep", jobRepository)
                .<ReturnFileEntry, ReturnFileEntry>chunk(100, transactionManager)
                .reader(reconciliationItemReader)
                .writer(reconciliationItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<ReturnFileEntry> reconciliationItemReader(
            PaymentReconciliationInputProvider provider,
            @Value("#{jobParameters['stagingToken']}") String stagingToken) {
        List<ReturnFileEntry> entries = provider.loadEntries(stagingToken);
        return new ListItemReader<>(entries);
    }

    @Bean
    public ItemWriter<ReturnFileEntry> reconciliationItemWriter(ReconciliationService reconciliationService) {
        return items -> reconciliationService.processEntries(List.copyOf(items.getItems()));
    }
}