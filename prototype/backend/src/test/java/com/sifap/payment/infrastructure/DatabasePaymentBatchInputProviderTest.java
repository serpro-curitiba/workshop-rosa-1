package com.sifap.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import com.sifap.beneficiary.domain.BeneficiaryStatus;
import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.payment.application.BatchPaymentInput;
import com.sifap.payment.domain.ProgramType;
import com.sifap.program.domain.ProgramEntity;
import com.sifap.program.infrastructure.ProgramRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DatabasePaymentBatchInputProviderTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private DatabasePaymentBatchInputProvider provider;

    @Test
    void should_map_beneficiaries_into_batch_inputs() {
        UUID programId = UUID.randomUUID();
        ProgramEntity program = ProgramEntity.withIdAndType(programId, com.sifap.program.domain.ProgramType.ASSISTENCIAL);
        setProgramBaseValue(program, new BigDecimal("1000.00"));
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(30),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("500.00"));

        given(programRepository.findAll(PageRequest.of(0, 1))).willReturn(new PageImpl<>(List.of(program)));
        given(beneficiaryRepository.findAll()).willReturn(List.of(beneficiary));

        List<BatchPaymentInput> result = provider.loadForReferenceMonth("202607");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).referenceYearMonth()).isEqualTo("202607");
        assertThat(result.get(0).programId()).isEqualTo(programId);
        assertThat(result.get(0).programType()).isEqualTo(ProgramType.A);
        assertThat(result.get(0).baseValue()).isEqualByComparingTo("1000.00");
    }

    private static void setProgramBaseValue(ProgramEntity entity, BigDecimal value) {
        try {
            var field = ProgramEntity.class.getDeclaredField("baseValue");
            field.setAccessible(true);
            field.set(entity, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}