package com.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sifap.payment.domain.Discount;
import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.domain.ProgramType;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentCalculationServiceEquivalenceTest {

    private static final BigDecimal BATCH_DISCOUNT_FACTOR = new BigDecimal("0.03");
    private static final Money BATCH_THRESHOLD = Money.of(new BigDecimal("500.00"));

    private final PaymentCalculationService paymentCalculationService = new PaymentCalculationService();
    private final DiscountService discountService = new DiscountService();
    private final CorrectionService correctionService = new CorrectionService();

    @Test
    void should_match_legacy_cases_from_csv() throws IOException {
        List<LegacyCase> cases = loadCases();
        assertThat(cases).hasSizeGreaterThanOrEqualTo(20);

        for (LegacyCase legacyCase : cases) {
            PaymentCalculationResult calculation = paymentCalculationService.calculateGrossAmount(
                    new PaymentCalculationCommand(
                            Money.of(legacyCase.baseValue()),
                            legacyCase.regionalFactor(),
                            legacyCase.familyFactor(),
                            legacyCase.incomeFactor(),
                            legacyCase.ageFactor(),
                            YearMonth.parse(legacyCase.referenceYearMonth(), DateTimeFormatter.ofPattern("yyyyMM")),
                            legacyCase.programType()));

            Money grossAmount = calculation.totalGrossAmount();
            List<Discount> discounts = new ArrayList<>();
            if (legacyCase.discountType() != null) {
                discounts.add(new Discount(legacyCase.discountType(), Money.of(legacyCase.discountAmount())));
            }
            if (legacyCase.batchDiscount() && grossAmount.isGreaterThan(BATCH_THRESHOLD)) {
                discounts.add(new Discount(DiscountType.OUTROS, grossAmount.multiply(BATCH_DISCOUNT_FACTOR)));
            }

            AppliedDiscounts appliedDiscounts = discountService.calculateAppliedDiscounts(grossAmount, discounts);
            Money netAmount = grossAmount.subtract(appliedDiscounts.totalApplied());

            PaymentEntity payment = PaymentEntity.create(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    legacyCase.referenceYearMonth(),
                    grossAmount,
                    netAmount,
                    legacyCase.correctionStatus());

            boolean correctionApplied = correctionService.applyCorrection(payment, Money.of(legacyCase.correctionDelta()));

            assertThat(grossAmount.amount())
                    .as("gross mismatch for case %s", legacyCase.caseId())
                    .isEqualByComparingTo(legacyCase.expectedGross());
            assertThat(payment.getNetAmount())
                    .as("net mismatch for case %s", legacyCase.caseId())
                    .isEqualByComparingTo(legacyCase.expectedNet());
            assertThat(correctionApplied)
                    .as("correction flag mismatch for case %s", legacyCase.caseId())
                    .isEqualTo(legacyCase.expectedCorrectionApplied());
        }
    }

    private List<LegacyCase> loadCases() throws IOException {
        String csv = new String(
                getClass().getClassLoader().getResourceAsStream("test-data/payment-calculation-legacy-cases.csv")
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        return csv.lines()
                .skip(1)
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(this::parseCase)
                .toList();
    }

    private LegacyCase parseCase(String line) {
        String[] parts = line.split(",");
        DiscountType discountType = parts[8].isBlank() ? null : DiscountType.valueOf(parts[8]);

        return new LegacyCase(
                parts[0],
                parts[1],
                ProgramType.valueOf(parts[2]),
                new BigDecimal(parts[3]),
                new BigDecimal(parts[4]),
                new BigDecimal(parts[5]),
                new BigDecimal(parts[6]),
                new BigDecimal(parts[7]),
                discountType,
                new BigDecimal(parts[9]),
                Boolean.parseBoolean(parts[10]),
                PaymentStatus.valueOf(parts[11]),
                new BigDecimal(parts[12]),
                new BigDecimal(parts[13]),
                new BigDecimal(parts[14]),
                Boolean.parseBoolean(parts[15]));
    }

    private record LegacyCase(
            String caseId,
            String referenceYearMonth,
            ProgramType programType,
            BigDecimal baseValue,
            BigDecimal regionalFactor,
            BigDecimal familyFactor,
            BigDecimal incomeFactor,
            BigDecimal ageFactor,
            DiscountType discountType,
            BigDecimal discountAmount,
            boolean batchDiscount,
            PaymentStatus correctionStatus,
            BigDecimal correctionDelta,
            BigDecimal expectedGross,
            BigDecimal expectedNet,
            boolean expectedCorrectionApplied) {
    }
}