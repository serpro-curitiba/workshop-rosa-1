package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.infrastructure.PaymentRepository;
import com.sifap.shared.audit.AuditEventPublisher;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void should_mark_payment_as_reconciled_when_difference_is_less_or_equal_to_tolerance() {
        UUID paymentId = UUID.randomUUID();
        PaymentEntity payment = PaymentEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202606",
                Money.of(new BigDecimal("100.00")),
                Money.of(new BigDecimal("100.00")),
                PaymentStatus.PENDING);
        setPaymentId(payment, paymentId);

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(any(PaymentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResult result = reconciliationService.processEntry(
                new ReturnFileEntry(paymentId, new BigDecimal("99.99")));

        assertThat(result.status()).isEqualTo(PaymentStatus.RECONCILED);
        verify(auditEventPublisher).publish(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void should_mark_payment_as_divergent_when_difference_exceeds_tolerance() {
        UUID paymentId = UUID.randomUUID();
        PaymentEntity payment = PaymentEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202606",
                Money.of(new BigDecimal("100.00")),
                Money.of(new BigDecimal("100.00")),
                PaymentStatus.PENDING);
        setPaymentId(payment, paymentId);

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(any(PaymentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResult result = reconciliationService.processEntry(
                new ReturnFileEntry(paymentId, new BigDecimal("99.98")));

        assertThat(result.status()).isEqualTo(PaymentStatus.DIVERGENT);
        verify(auditEventPublisher).publish(any(), any(), any(), any(), any(), any(), any());
    }

    private static void setPaymentId(PaymentEntity entity, UUID id) {
        try {
            var field = PaymentEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}