package com.sifap.payment.application;

import com.sifap.payment.domain.Discount;
import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.infrastructure.PaymentRepository;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentBatchService {

    private static final BigDecimal SIMPLIFIED_BATCH_DISCOUNT_FACTOR = new BigDecimal("0.03");
    private static final Money SIMPLIFIED_BATCH_THRESHOLD = Money.of(new BigDecimal("500.00"));

    private final PaymentCalculationService paymentCalculationService;
    private final DiscountService discountService;
    private final PaymentRepository paymentRepository;

    public PaymentBatchService(
            PaymentCalculationService paymentCalculationService,
            DiscountService discountService,
            PaymentRepository paymentRepository) {
        this.paymentCalculationService = paymentCalculationService;
        this.discountService = discountService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public List<PaymentEntity> generateCycle(List<BatchPaymentInput> items) {
        return items.stream()
                .map(this::processItem)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }

    @Transactional
    public java.util.Optional<PaymentEntity> processItem(BatchPaymentInput input) {
        if (paymentRepository.findByBeneficiaryIdAndReferenceYearMonth(input.beneficiaryId(), input.referenceYearMonth())
                .isPresent()) {
            return java.util.Optional.empty();
        }

        PaymentCalculationResult calculation = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(input.baseValue()),
                        input.regionalFactor(),
                        input.familyFactor(),
                        input.incomeFactor(),
                        input.ageFactor(),
                        YearMonth.parse(input.referenceYearMonth(), DateTimeFormatter.ofPattern("yyyyMM")),
                        input.programType()));

        Money grossAmount = calculation.totalGrossAmount();
        List<Discount> allDiscounts = new ArrayList<>();
        if (input.discounts() != null) {
            allDiscounts.addAll(input.discounts().stream()
                    .map(discount -> new Discount(discount.type(), Money.of(discount.amount())))
                    .toList());
        }

        if (grossAmount.isGreaterThan(SIMPLIFIED_BATCH_THRESHOLD)) {
            allDiscounts.add(new Discount(
                    DiscountType.OUTROS,
                    grossAmount.multiply(SIMPLIFIED_BATCH_DISCOUNT_FACTOR)));
        }

        AppliedDiscounts appliedDiscounts = discountService.validateAndCalculateAppliedDiscounts(grossAmount, allDiscounts);
        Money netAmount = grossAmount.subtract(appliedDiscounts.totalApplied());

        return java.util.Optional.of(paymentRepository.save(PaymentEntity.create(
                input.beneficiaryId(),
                input.programId(),
                input.referenceYearMonth(),
                grossAmount,
                netAmount,
                PaymentStatus.PENDING)));
    }
}