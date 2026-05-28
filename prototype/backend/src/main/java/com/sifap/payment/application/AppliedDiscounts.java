package com.sifap.payment.application;

import com.sifap.shared.domain.Money;

public record AppliedDiscounts(
        Money judicialTotal,
        Money nonJudicialRequestedTotal,
        Money nonJudicialAppliedTotal,
        Money totalApplied,
        boolean nonJudicialCapExceeded) {
}