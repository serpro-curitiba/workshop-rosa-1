package com.sifap.beneficiary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DocumentValidationPolicyTest {

    private final DocumentValidationPolicy policy = new DocumentValidationPolicy(prefix -> prefix.equals("999"));

    @Test
    void should_accept_valid_cpf() {
        assertThat(policy.validateAndNormalizeCpf("529.982.247-25")).isEqualTo("52998224725");
    }

    @Test
    void should_reject_cpf_with_all_equal_digits() {
        assertThatThrownBy(() -> policy.validateAndNormalizeCpf("111.111.111-11"))
                .isInstanceOf(InvalidCpfException.class)
                .hasMessage("CPF with all repeated digits is not allowed");
    }

    @Test
    void should_reject_invalid_check_digits() {
        assertThatThrownBy(() -> policy.validateAndNormalizeCpf("529.982.247-26"))
                .isInstanceOf(InvalidCpfException.class)
                .hasMessage("CPF check digits are invalid");
    }

    @Test
    void should_accept_special_prefix_without_check_digit_validation() {
        assertThat(policy.validateAndNormalizeCpf("999.000.000-00")).isEqualTo("99900000000");
    }
}