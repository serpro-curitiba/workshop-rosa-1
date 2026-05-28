package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.payment.domain.PaymentEntity;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CorrectionServiceTest {

    private final CorrectionService correctionService = new CorrectionService();

    @Test
    void should_apply_correction_when_payment_is_approved_and_not_corrected() {
        PaymentEntity payment = PaymentEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202606",
                Money.of(new BigDecimal("1000.00")),
                Money.of(new BigDecimal("1000.00")),
                PaymentStatus.APPROVED);

        boolean applied = correctionService.applyCorrection(payment, Money.of(new BigDecimal("12.34")));

        assertThat(applied).isTrue();
        assertThat(payment.getNetAmount()).isEqualByComparingTo("1012.34");
        assertThat(payment.isCorrected()).isTrue();
    }

    @Test
    void should_not_apply_correction_twice_for_same_payment() {
        PaymentEntity payment = PaymentEntity.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202606",
                Money.of(new BigDecimal("1000.00")),
                Money.of(new BigDecimal("1000.00")),
                PaymentStatus.APPROVED);

        boolean first = correctionService.applyCorrection(payment, Money.of(new BigDecimal("10.00")));
        boolean second = correctionService.applyCorrection(payment, Money.of(new BigDecimal("10.00")));

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(payment.getNetAmount()).isEqualByComparingTo("1010.00");
    }
}