package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.payment.domain.ProgramType;
import com.sifap.shared.domain.Money;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class PaymentCalculationServiceTest {

    private final PaymentCalculationService paymentCalculationService = new PaymentCalculationService();

    @Test
    void should_calculate_multiparametric_gross_amount_with_truncation_at_each_step() {
        PaymentCalculationResult result = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(new BigDecimal("1000.00")),
                        new BigDecimal("1.10"),
                        new BigDecimal("1.03"),
                        new BigDecimal("0.95"),
                        new BigDecimal("1.02"),
                        YearMonth.of(2026, 5),
                        ProgramType.B));

        assertThat(result.baseGrossAmount().amount()).isEqualByComparingTo("1097.87");
        assertThat(result.totalGrossAmount().amount()).isEqualByComparingTo("1097.87");
        assertThat(result.thirteenthAmount().amount()).isEqualByComparingTo("0.00");
        assertThat(result.christmasBonusAmount().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_add_thirteenth_and_christmas_bonus_in_december_for_program_type_a() {
        PaymentCalculationResult result = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(new BigDecimal("1000.00")),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        YearMonth.of(2026, 12),
                        ProgramType.A));

        assertThat(result.baseGrossAmount().amount()).isEqualByComparingTo("1000.00");
        assertThat(result.thirteenthAmount().amount()).isEqualByComparingTo("1000.00");
        assertThat(result.christmasBonusAmount().amount()).isEqualByComparingTo("150.00");
        assertThat(result.totalGrossAmount().amount()).isEqualByComparingTo("2150.00");
    }

    @Test
    void should_not_add_seasonal_benefit_in_december_for_non_type_a_program() {
        PaymentCalculationResult result = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(new BigDecimal("1000.00")),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        YearMonth.of(2026, 12),
                        ProgramType.B));

        assertThat(result.totalGrossAmount().amount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void should_not_add_seasonal_benefit_outside_december_even_for_program_type_a() {
        PaymentCalculationResult result = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(new BigDecimal("1000.00")),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        YearMonth.of(2026, 1),
                        ProgramType.A));

        assertThat(result.totalGrossAmount().amount()).isEqualByComparingTo("1000.00");
    }
}