package com.sifap.program.application;

import com.sifap.program.domain.ProgramType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProgramView(
        UUID id,
        String name,
        ProgramType type,
        BigDecimal baseValue,
        BigDecimal adjustmentFactor,
        BigDecimal fatorK,
        String status) {
}