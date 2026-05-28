package com.sifap.payment.domain;

import com.sifap.shared.domain.Money;

import java.util.Objects;

public record Discount(DiscountType type, Money amount) {

    public Discount {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }

    public boolean isJudicial() {
        return type == DiscountType.JUDICIAL;
    }
}