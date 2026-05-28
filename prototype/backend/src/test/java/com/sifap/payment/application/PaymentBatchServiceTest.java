package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sifap.payment.domain.Discount;
import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.domain.ProgramType;
import com.sifap.payment.infrastructure.PaymentRepository;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentBatchServiceTest {

    @Mock
    private PaymentCalculationService paymentCalculationService;

    @Mock
    private DiscountService discountService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentBatchService paymentBatchService;

    @Test
    void should_apply_simplified_three_percent_discount_when_gross_is_above_five_hundred() {
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        BatchPaymentInput input = new BatchPaymentInput(
                beneficiaryId,
                programId,
                "202606",
                new BigDecimal("1000.00"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                ProgramType.B,
                List.of());

        given(paymentRepository.findByBeneficiaryIdAndReferenceYearMonth(beneficiaryId, "202606"))
                .willReturn(Optional.empty());
        given(paymentCalculationService.calculateGrossAmount(any())).willReturn(new PaymentCalculationResult(
                Money.of(new BigDecimal("600.00")),
                Money.zero(),
                Money.zero(),
                Money.of(new BigDecimal("600.00"))));
        given(discountService.validateAndCalculateAppliedDiscounts(any(), any())).willReturn(new AppliedDiscounts(
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Money.of(new BigDecimal("18.00")),
                false));
        given(paymentRepository.save(any(PaymentEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        Optional<PaymentEntity> result = paymentBatchService.processItem(input);

        assertThat(result).isPresent();
        assertThat(result.get().getNetAmount()).isEqualByComparingTo("582.00");
        assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.PENDING);

        ArgumentCaptor<List<Discount>> discountsCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(discountService).validateAndCalculateAppliedDiscounts(any(), discountsCaptor.capture());
        assertThat(discountsCaptor.getValue())
                .anySatisfy(discount -> {
                    assertThat(discount.type()).isEqualTo(DiscountType.OUTROS);
                    assertThat(discount.amount().amount()).isEqualByComparingTo("18.00");
                });
    }

    @Test
    void should_skip_generation_when_payment_for_month_already_exists() {
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        BatchPaymentInput input = new BatchPaymentInput(
                beneficiaryId,
                programId,
                "202606",
                new BigDecimal("1000.00"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                ProgramType.A,
                List.of());
        given(paymentRepository.findByBeneficiaryIdAndReferenceYearMonth(beneficiaryId, "202606"))
                .willReturn(Optional.of(PaymentEntity.create(
                        beneficiaryId,
                        programId,
                        "202606",
                        Money.of(new BigDecimal("100.00")),
                        Money.of(new BigDecimal("90.00")),
                        PaymentStatus.PENDING)));

        Optional<PaymentEntity> result = paymentBatchService.processItem(input);

        assertThat(result).isEmpty();
    }
}