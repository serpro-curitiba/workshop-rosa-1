package com.sifap.payment.application;

import com.sifap.payment.domain.Discount;
import com.sifap.payment.domain.PaymentEntity;
import com.sifap.payment.infrastructure.PaymentRepository;
import com.sifap.shared.domain.Money;
import com.sifap.shared.domain.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCycleService {

    private final PaymentCalculationService paymentCalculationService;
    private final DiscountService discountService;
    private final PaymentRepository paymentRepository;

    public PaymentCycleService(
            PaymentCalculationService paymentCalculationService,
            DiscountService discountService,
            PaymentRepository paymentRepository) {
        this.paymentCalculationService = paymentCalculationService;
        this.discountService = discountService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentView generatePayment(CreatePaymentCycleCommand command) {
        paymentRepository.findByBeneficiaryIdAndReferenceYearMonth(command.beneficiaryId(), command.referenceYearMonth())
                .ifPresent(existing -> {
                    throw new DuplicatePaymentException("Payment already exists for beneficiary and reference month");
                });

        PaymentCalculationResult calculationResult = paymentCalculationService.calculateGrossAmount(
                new PaymentCalculationCommand(
                        Money.of(command.baseValue()),
                        command.regionalFactor(),
                        command.familyFactor(),
                        command.incomeFactor(),
                        command.ageFactor(),
                        java.time.YearMonth.parse(command.referenceYearMonth(), java.time.format.DateTimeFormatter.ofPattern("yyyyMM")),
                        command.programType()));

        AppliedDiscounts appliedDiscounts = discountService.validateAndCalculateAppliedDiscounts(
                calculationResult.totalGrossAmount(),
                command.discounts().stream()
                        .map(discount -> new Discount(discount.type(), Money.of(discount.amount())))
                        .toList());

        Money netAmount = calculationResult.totalGrossAmount().subtract(appliedDiscounts.totalApplied());
        PaymentEntity paymentEntity = paymentRepository.save(
                PaymentEntity.create(
                        command.beneficiaryId(),
                        command.programId(),
                        command.referenceYearMonth(),
                        calculationResult.totalGrossAmount(),
                        netAmount,
                        PaymentStatus.PENDING));

        return toView(paymentEntity);
    }

    @Transactional(readOnly = true)
    public List<PaymentView> listCycle(String referenceYearMonth) {
        return paymentRepository.findAllByReferenceYearMonthOrderByCreatedAtAsc(referenceYearMonth).stream()
                .map(this::toView)
                .toList();
    }

        @Transactional
        public PaymentView approvePayment(java.util.UUID paymentId) {
                return updatePaymentStatus(paymentId, PaymentStatus.APPROVED);
        }

        @Transactional
        public PaymentView rejectPayment(java.util.UUID paymentId) {
                return updatePaymentStatus(paymentId, PaymentStatus.REJECTED);
        }

        private PaymentView updatePaymentStatus(java.util.UUID paymentId, PaymentStatus targetStatus) {
                PaymentEntity payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

                if (payment.getStatus() != PaymentStatus.PENDING) {
                        throw new PaymentStatusTransitionException("Payment status transition is invalid");
                }

                payment.updateStatus(targetStatus);
                return toView(paymentRepository.save(payment));
        }

    private PaymentView toView(PaymentEntity paymentEntity) {
        return new PaymentView(
                paymentEntity.getId(),
                paymentEntity.getBeneficiaryId(),
                paymentEntity.getProgramId(),
                paymentEntity.getReferenceYearMonth(),
                paymentEntity.getGrossAmount(),
                paymentEntity.getNetAmount(),
                paymentEntity.getStatus());
    }
}