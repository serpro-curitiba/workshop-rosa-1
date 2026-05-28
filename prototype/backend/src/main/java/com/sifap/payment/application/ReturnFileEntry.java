package com.sifap.payment.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnFileEntry(UUID paymentId, BigDecimal bankAmount) {
}