package com.sifap.payment.application;

import com.sifap.payment.domain.ProgramType;
import com.sifap.shared.domain.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Objects;

@Service
public class PaymentCalculationService {

    private static final BigDecimal CHRISTMAS_BONUS_FACTOR = new BigDecimal("0.15");

    public PaymentCalculationResult calculateGrossAmount(PaymentCalculationCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Money baseGrossAmount = command.baseValue()
                .multiply(command.regionalFactor())
                .multiply(command.familyFactor())
                .multiply(command.incomeFactor())
                .multiply(command.ageFactor());

        if (command.referenceMonth().getMonth() == Month.DECEMBER && command.programType() == ProgramType.A) {
            Money thirteenthAmount = baseGrossAmount;
            Money christmasBonusAmount = baseGrossAmount.multiply(CHRISTMAS_BONUS_FACTOR);
            Money totalGrossAmount = baseGrossAmount.add(thirteenthAmount).add(christmasBonusAmount);
            return new PaymentCalculationResult(
                    baseGrossAmount,
                    thirteenthAmount,
                    christmasBonusAmount,
                    totalGrossAmount);
        }

        return new PaymentCalculationResult(baseGrossAmount, Money.zero(), Money.zero(), baseGrossAmount);
    }
}