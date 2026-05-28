package com.sifap.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void should_truncate_input_when_creating_money() {
        assertThat(Money.of(new BigDecimal("99.999")).amount())
                .isEqualByComparingTo("99.99");
    }

    @Test
    void should_keep_two_decimals_when_input_has_single_decimal() {
        assertThat(Money.of(new BigDecimal("10.5")).amount())
                .isEqualByComparingTo("10.50");
    }

    @Test
    void should_truncate_sum_result_when_adding_values() {
        Money result = Money.of(new BigDecimal("10.115"))
                .add(Money.of(new BigDecimal("1.119")));

        assertThat(result.amount()).isEqualByComparingTo("11.22");
    }

    @Test
    void should_truncate_difference_when_subtracting_values() {
        Money result = Money.of(new BigDecimal("10.999"))
                .subtract(Money.of(new BigDecimal("0.554")));

        assertThat(result.amount()).isEqualByComparingTo("10.44");
    }

    @Test
    void should_truncate_result_when_multiplying_values() {
        Money result = Money.of(new BigDecimal("1.555"))
                .multiply(new BigDecimal("2"));

        assertThat(result.amount()).isEqualByComparingTo("3.11");
    }

    @Test
    void should_truncate_fractional_result_when_multiplying_by_decimal_factor() {
        Money result = Money.of(new BigDecimal("100.00"))
                .multiply(new BigDecimal("0.347215"));

        assertThat(result.amount()).isEqualByComparingTo("34.72");
    }

    @Test
    void should_create_zero_money() {
        assertThat(Money.zero().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_report_positive_only_for_values_above_zero() {
        assertThat(Money.of(new BigDecimal("0.01")).isPositive()).isTrue();
        assertThat(Money.zero().isPositive()).isFalse();
    }

    @Test
    void should_compare_values_using_amount() {
        assertThat(Money.of(new BigDecimal("10.00"))
                .isGreaterThan(Money.of(new BigDecimal("9.99"))))
                .isTrue();
    }
}