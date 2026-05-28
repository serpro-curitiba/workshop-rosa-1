package com.sifap.program.application;

import com.sifap.program.domain.ProgramEntity;
import com.sifap.program.domain.ProgramNotFoundException;
import com.sifap.program.infrastructure.ProgramRepository;
import com.sifap.shared.domain.Money;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;

    @Value("${sifap.payment.fator-k:0.347215}")
    private BigDecimal fatorK;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @PostConstruct
    void validateFatorKConfiguration() {
        if (fatorK.compareTo(BigDecimal.ZERO) <= 0 || fatorK.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException("sifap.payment.fator-k must be greater than 0 and less than or equal to 1");
        }
    }

    public Money calculateAdjustedBaseValue(Money baseValue, BigDecimal adjustmentFactor) {
        BigDecimal multiplier = BigDecimal.ONE.add(adjustmentFactor.multiply(fatorK));
        return baseValue.multiply(multiplier);
    }

    @Transactional(readOnly = true)
    public List<ProgramView> listPrograms() {
        return programRepository.findAll().stream().map(this::toView).toList();
    }

    @Transactional
    public ProgramView updateFatorK(UUID programId, BigDecimal newFatorK) {
        if (newFatorK.compareTo(BigDecimal.ZERO) <= 0 || newFatorK.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("fator_k must be greater than 0 and less than or equal to 1");
        }
        ProgramEntity program = programRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException("Program not found"));
        program.updateFatorK(newFatorK);
        return toView(programRepository.save(program));
    }

    private ProgramView toView(ProgramEntity program) {
        return new ProgramView(
                program.getId(),
                program.getName(),
                program.getType(),
                program.getBaseValue(),
                program.getAdjustmentFactor(),
                program.getFatorK(),
                program.getStatus());
    }
}