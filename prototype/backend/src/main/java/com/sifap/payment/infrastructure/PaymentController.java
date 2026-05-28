package com.sifap.payment.infrastructure;

import com.sifap.payment.application.CreatePaymentCycleCommand;
import com.sifap.payment.application.PaymentCycleService;
import com.sifap.payment.application.PaymentView;
import com.sifap.payment.domain.DiscountType;
import com.sifap.payment.domain.ProgramType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "payment")
public class PaymentController {

    private final PaymentCycleService paymentCycleService;

    public PaymentController(PaymentCycleService paymentCycleService) {
        this.paymentCycleService = paymentCycleService;
    }

    @PostMapping("/payment-cycles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a payment for the requested cycle")
    public PaymentView createPaymentCycle(@Valid @RequestBody CreatePaymentCycleRequest request) {
        return paymentCycleService.generatePayment(request.toCommand());
    }

    @GetMapping("/payment-cycles/{referenceYearMonth}")
    @Operation(summary = "List generated payments for a cycle")
    public List<PaymentView> listCycle(@PathVariable @Pattern(regexp = "\\d{6}") String referenceYearMonth) {
        return paymentCycleService.listCycle(referenceYearMonth);
    }

    @PatchMapping("/payments/{paymentId}/approve")
    @Operation(summary = "Approve a pending payment")
    public PaymentView approvePayment(@PathVariable UUID paymentId) {
        return paymentCycleService.approvePayment(paymentId);
    }

    @PatchMapping("/payments/{paymentId}/reject")
    @Operation(summary = "Reject a pending payment")
    public PaymentView rejectPayment(@PathVariable UUID paymentId) {
        return paymentCycleService.rejectPayment(paymentId);
    }

    public record CreatePaymentCycleRequest(
            @NotNull UUID beneficiaryId,
            @NotNull UUID programId,
            @NotBlank @Pattern(regexp = "\\d{6}") String referenceYearMonth,
            @NotNull @DecimalMin("0.00") BigDecimal baseValue,
            @NotNull @DecimalMin("0.00") BigDecimal regionalFactor,
            @NotNull @DecimalMin("0.00") BigDecimal familyFactor,
            @NotNull @DecimalMin("0.00") BigDecimal incomeFactor,
            @NotNull @DecimalMin("0.00") BigDecimal ageFactor,
            @NotNull ProgramType programType,
            List<CreatePaymentDiscountRequest> discounts) {

        CreatePaymentCycleCommand toCommand() {
            List<CreatePaymentCycleCommand.CreatePaymentDiscountCommand> mappedDiscounts = discounts == null
                    ? List.of()
                    : discounts.stream()
                            .map(discount -> new CreatePaymentCycleCommand.CreatePaymentDiscountCommand(
                                    discount.type(),
                                    discount.amount()))
                            .toList();

            return new CreatePaymentCycleCommand(
                    beneficiaryId,
                    programId,
                    referenceYearMonth,
                    baseValue,
                    regionalFactor,
                    familyFactor,
                    incomeFactor,
                    ageFactor,
                    programType,
                    mappedDiscounts);
        }
    }

    public record CreatePaymentDiscountRequest(
            @NotNull DiscountType type,
            @NotNull @DecimalMin("0.00") BigDecimal amount) {
    }
}