package com.sifap.payment.application;

import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.infrastructure.PaymentRepository;
import com.sifap.shared.audit.AuditEventPublisher;
import com.sifap.shared.domain.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReconciliationService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final PaymentRepository paymentRepository;
    private final AuditEventPublisher auditEventPublisher;

    public ReconciliationService(PaymentRepository paymentRepository, AuditEventPublisher auditEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public List<ReconciliationResult> processEntries(List<ReturnFileEntry> entries) {
        return entries.stream().map(this::processEntry).toList();
    }

    @Transactional
    public ReconciliationResult processEntry(ReturnFileEntry entry) {
        PaymentEntity payment = paymentRepository.findById(entry.paymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for reconciliation"));

        PaymentStatus previousStatus = payment.getStatus();
        BigDecimal delta = payment.getNetAmount().subtract(entry.bankAmount()).abs();
        PaymentStatus newStatus = delta.compareTo(TOLERANCE) <= 0 ? PaymentStatus.RECONCILED : PaymentStatus.DIVERGENT;
        payment.updateStatus(newStatus);
        paymentRepository.save(payment);

        auditEventPublisher.publish(
                "Payment",
                payment.getId(),
                "STATUS_CHANGE",
                previousStatus.name(),
                newStatus.name(),
                "BATCH_PROCESS",
                "Reconciliation return file");

        return new ReconciliationResult(payment.getId(), newStatus, previousStatus != newStatus);
    }
}