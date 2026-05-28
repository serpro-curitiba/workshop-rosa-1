package com.sifap.payment.application;

import com.sifap.payment.domain.PaymentEntity;
import com.sifap.shared.domain.Money;
import org.springframework.stereotype.Service;

@Service
public class CorrectionService {

    public boolean applyCorrection(PaymentEntity payment, Money delta) {
        return payment.applyCorrection(delta);
    }
}