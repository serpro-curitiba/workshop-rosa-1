package com.sifap.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.payment.domain.DiscountEntity;
import com.sifap.payment.domain.DiscountType;
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
class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_find_discounts_by_payment_id() {
        UUID beneficiaryId = insertBeneficiary("32165498700");
        UUID programId = insertProgram();
        UUID paymentId = UUID.randomUUID();
        insertPayment(paymentId, beneficiaryId, programId);

        discountRepository.saveAndFlush(DiscountEntity.create(paymentId, DiscountType.OUTROS, new BigDecimal("10.00")));
        discountRepository.saveAndFlush(DiscountEntity.create(paymentId, DiscountType.JUDICIAL, new BigDecimal("20.00")));

        assertThat(discountRepository.findByPaymentId(paymentId))
                .hasSize(2)
                .extracting(DiscountEntity::getType)
                .containsExactlyInAnyOrder(DiscountType.OUTROS, DiscountType.JUDICIAL);
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
                "Beneficiary Discount Test",
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
                "Programa Desconto",
                "ASSISTENCIAL",
                new BigDecimal("1000.00"),
                new BigDecimal("1.000000"),
                new BigDecimal("0.347215"),
                "ACTIVE");
        return programId;
    }

    private void insertPayment(UUID paymentId, UUID beneficiaryId, UUID programId) {
        jdbcTemplate.update(
                """
                INSERT INTO payment (id, beneficiary_id, program_id, reference_year_month, gross_amount, net_amount, corrected, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                paymentId,
                beneficiaryId,
                programId,
                "202606",
                new BigDecimal("1000.00"),
                new BigDecimal("900.00"),
                false,
                "PENDING");
    }
}