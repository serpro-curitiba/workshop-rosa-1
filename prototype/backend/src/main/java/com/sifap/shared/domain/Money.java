package com.sifap.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
    }

    public static Money of(BigDecimal value) {
        return new Money(Objects.requireNonNull(value, "value must not be null"));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return Money.of(amount().add(other.amount()));
    }

    public Money subtract(Money other) {
        return Money.of(amount().subtract(other.amount()));
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        return Money.of(amount.multiply(factor));
    }

    public boolean isPositive() {
        return amount().signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        return amount().compareTo(other.amount()) > 0;
    }

    @Override
    public BigDecimal amount() {
        return amount.setScale(2, RoundingMode.DOWN);
    }
}