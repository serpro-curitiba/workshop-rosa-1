package com.sifap.payment.infrastructure;

import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.payment.application.BatchPaymentInput;
import com.sifap.payment.application.PaymentBatchInputProvider;
import com.sifap.payment.domain.ProgramType;
import com.sifap.program.infrastructure.ProgramRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Primary
public class DatabasePaymentBatchInputProvider implements PaymentBatchInputProvider {

    private final BeneficiaryRepository beneficiaryRepository;
    private final ProgramRepository programRepository;

    public DatabasePaymentBatchInputProvider(
            BeneficiaryRepository beneficiaryRepository,
            ProgramRepository programRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.programRepository = programRepository;
    }

    @Override
    public List<BatchPaymentInput> loadForReferenceMonth(String referenceYearMonth) {
        var maybeProgram = programRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst();
        if (maybeProgram.isEmpty()) {
            return List.of();
        }

        var program = maybeProgram.get();
        ProgramType mappedType = mapProgramType(program.getType());

        return beneficiaryRepository.findAll().stream()
                .map(beneficiary -> new BatchPaymentInput(
                        beneficiary.getId(),
                        program.getId(),
                        referenceYearMonth,
                        program.getBaseValue(),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        mappedType,
                        List.of()))
                .toList();
    }

    private ProgramType mapProgramType(com.sifap.program.domain.ProgramType type) {
        return type == com.sifap.program.domain.ProgramType.ASSISTENCIAL ? ProgramType.A : ProgramType.B;
    }
}