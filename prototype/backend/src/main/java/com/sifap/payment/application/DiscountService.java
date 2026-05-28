package com.sifap.payment.application;

import com.sifap.payment.domain.Discount;
import com.sifap.shared.domain.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class DiscountService {

    private static final BigDecimal NON_JUDICIAL_CAP_FACTOR = new BigDecimal("0.30");

    public AppliedDiscounts calculateAppliedDiscounts(Money grossAmount, List<Discount> discounts) {
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");

        if (discounts == null || discounts.isEmpty()) {
            return new AppliedDiscounts(Money.zero(), Money.zero(), Money.zero(), Money.zero(), false);
        }

        Money judicialTotal = discounts.stream()
                .filter(Discount::isJudicial)
                .map(Discount::amount)
                .reduce(Money.zero(), Money::add);

        Money nonJudicialRequestedTotal = discounts.stream()
                .filter(discount -> !discount.isJudicial())
                .map(Discount::amount)
                .reduce(Money.zero(), Money::add);

        Money nonJudicialCap = grossAmount.multiply(NON_JUDICIAL_CAP_FACTOR);
        boolean exceeded = nonJudicialRequestedTotal.isGreaterThan(nonJudicialCap);
        Money nonJudicialAppliedTotal = exceeded ? nonJudicialCap : nonJudicialRequestedTotal;
        Money totalApplied = judicialTotal.add(nonJudicialAppliedTotal);

        return new AppliedDiscounts(
                judicialTotal,
                nonJudicialRequestedTotal,
                nonJudicialAppliedTotal,
                totalApplied,
                exceeded);
    }

    public AppliedDiscounts validateAndCalculateAppliedDiscounts(Money grossAmount, List<Discount> discounts) {
        AppliedDiscounts appliedDiscounts = calculateAppliedDiscounts(grossAmount, discounts);
        if (appliedDiscounts.nonJudicialCapExceeded()) {
            throw new DiscountCapExceededException(
                    "Non-judicial discounts exceed 30% of gross amount");
        }
        return appliedDiscounts;
    }
}