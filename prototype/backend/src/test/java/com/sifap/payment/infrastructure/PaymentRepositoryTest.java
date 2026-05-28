package com.sifap.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sifap.payment.domain.PaymentEntity;
import com.sifap.shared.domain.Money;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_find_payment_by_beneficiary_and_reference_month() {
        UUID beneficiaryId = insertBeneficiary("12345678901");
        UUID programId = insertProgram();
        PaymentEntity payment = paymentRepository.saveAndFlush(
                PaymentEntity.pending(beneficiaryId, programId, "202605", Money.of(new BigDecimal("1000.00"))));

        assertThat(paymentRepository.findByBeneficiaryIdAndReferenceYearMonth(beneficiaryId, "202605"))
                .hasValueSatisfying(found -> {
                    assertThat(found.getId()).isEqualTo(payment.getId());
                    assertThat(found.getStatus().name()).isEqualTo("PENDING");
                });
    }

    @Test
    void should_reject_duplicate_payment_for_same_beneficiary_and_reference_month() {
        UUID beneficiaryId = insertBeneficiary("98765432100");
        UUID firstProgramId = insertProgram();
        UUID secondProgramId = insertProgram();

        paymentRepository.saveAndFlush(
                PaymentEntity.pending(beneficiaryId, firstProgramId, "202606", Money.of(new BigDecimal("800.00"))));

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(
                PaymentEntity.pending(beneficiaryId, secondProgramId, "202606", Money.of(new BigDecimal("900.00")))))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private UUID insertBeneficiary(String cpf) {
        UUID beneficiaryId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO beneficiary (id, cpf, name, birth_date, status, cod_region, family_members, family_income)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                beneficiaryId,
                cpf,
                "Beneficiary Test",
                Date.valueOf("1980-01-01"),
                "ACTIVE",
                1,
                3,
                new BigDecimal("400.00"));
        return beneficiaryId;
    }

    private UUID insertProgram() {
        UUID programId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO program (id, name, type, base_value, adjustment_factor, fator_k, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                programId,
                "Programa Teste",
                "ASSISTENCIAL",
                new BigDecimal("1000.00"),
                new BigDecimal("1.000000"),
                new BigDecimal("0.347215"),
                "ACTIVE");
        return programId;
    }
}