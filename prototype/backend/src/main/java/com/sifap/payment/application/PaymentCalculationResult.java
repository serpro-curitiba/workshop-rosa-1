package com.sifap.payment.application;

import com.sifap.shared.domain.Money;

public record PaymentCalculationResult(
        Money baseGrossAmount,
        Money thirteenthAmount,
        Money christmasBonusAmount,
        Money totalGrossAmount) {
}