package com.sifap.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PaymentStatusTest {

    @Test
    void should_expose_only_canonical_status_values() {
        assertThat(PaymentStatus.values())
                .containsExactly(
                        PaymentStatus.PENDING,
                        PaymentStatus.APPROVED,
                        PaymentStatus.REJECTED,
                        PaymentStatus.RECONCILED,
                        PaymentStatus.DIVERGENT,
                        PaymentStatus.CANCELLED);
    }

    @Test
    void should_reject_legacy_alias_values() {
        assertThatThrownBy(() -> PaymentStatus.valueOf("X"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}