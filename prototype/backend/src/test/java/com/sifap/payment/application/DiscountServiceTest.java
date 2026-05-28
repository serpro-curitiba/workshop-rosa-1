package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sifap.payment.domain.Discount;
import com.sifap.payment.domain.DiscountType;
import com.sifap.shared.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscountServiceTest {

    private final DiscountService discountService = new DiscountService();

    @Test
    void should_cap_non_judicial_discounts_at_thirty_percent_of_gross() {
        AppliedDiscounts result = discountService.calculateAppliedDiscounts(
                Money.of(new BigDecimal("1000.00")),
                List.of(new Discount(DiscountType.OUTROS, Money.of(new BigDecimal("400.00")))));

        assertThat(result.nonJudicialAppliedTotal().amount()).isEqualByComparingTo("300.00");
        assertThat(result.totalApplied().amount()).isEqualByComparingTo("300.00");
        assertThat(result.nonJudicialCapExceeded()).isTrue();
    }

    @Test
    void should_keep_non_judicial_discounts_when_total_is_below_cap() {
        AppliedDiscounts result = discountService.calculateAppliedDiscounts(
                Money.of(new BigDecimal("1000.00")),
                List.of(new Discount(DiscountType.PREVIDENCIARIO, Money.of(new BigDecimal("200.00")))));

        assertThat(result.nonJudicialAppliedTotal().amount()).isEqualByComparingTo("200.00");
        assertThat(result.totalApplied().amount()).isEqualByComparingTo("200.00");
        assertThat(result.nonJudicialCapExceeded()).isFalse();
    }

    @Test
    void should_apply_judicial_discount_without_cap() {
        AppliedDiscounts result = discountService.calculateAppliedDiscounts(
                Money.of(new BigDecimal("1000.00")),
                List.of(new Discount(DiscountType.JUDICIAL, Money.of(new BigDecimal("800.00")))));

        assertThat(result.judicialTotal().amount()).isEqualByComparingTo("800.00");
        assertThat(result.totalApplied().amount()).isEqualByComparingTo("800.00");
        assertThat(result.nonJudicialCapExceeded()).isFalse();
    }

    @Test
    void should_mix_judicial_and_non_judicial_discounts_using_cap_only_for_non_judicial() {
        AppliedDiscounts result = discountService.calculateAppliedDiscounts(
                Money.of(new BigDecimal("1000.00")),
                List.of(
                        new Discount(DiscountType.JUDICIAL, Money.of(new BigDecimal("400.00"))),
                        new Discount(DiscountType.SINDICATO, Money.of(new BigDecimal("400.00")))));

        assertThat(result.judicialTotal().amount()).isEqualByComparingTo("400.00");
        assertThat(result.nonJudicialAppliedTotal().amount()).isEqualByComparingTo("300.00");
        assertThat(result.totalApplied().amount()).isEqualByComparingTo("700.00");
    }

    @Test
    void should_throw_when_validation_path_detects_non_judicial_cap_violation() {
        assertThatThrownBy(() -> discountService.validateAndCalculateAppliedDiscounts(
                Money.of(new BigDecimal("1000.00")),
                List.of(new Discount(DiscountType.OUTROS, Money.of(new BigDecimal("350.00"))))))
                .isInstanceOf(DiscountCapExceededException.class)
                .hasMessage("Non-judicial discounts exceed 30% of gross amount");
    }
}