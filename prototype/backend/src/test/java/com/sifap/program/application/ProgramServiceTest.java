package com.sifap.program.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sifap.program.domain.ProgramEntity;
import com.sifap.program.infrastructure.ProgramRepository;
import com.sifap.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    private ProgramService programService;

    @BeforeEach
    void setUp() {
        programService = new ProgramService(programRepository);
        ReflectionTestUtils.setField(programService, "fatorK", new BigDecimal("0.347215"));
        ReflectionTestUtils.invokeMethod(programService, "validateFatorKConfiguration");
    }

    @Test
    void should_calculate_adjusted_base_value_using_externalized_fator_k() {
        Money result = programService.calculateAdjustedBaseValue(
                Money.of(new BigDecimal("1000.00")),
                new BigDecimal("0.10"));

        assertThat(result.amount()).isEqualByComparingTo("1034.72");
    }

    @Test
    void should_reject_invalid_fator_k_configuration() {
        ProgramService invalidService = new ProgramService(programRepository);
        ReflectionTestUtils.setField(invalidService, "fatorK", new BigDecimal("2.00"));

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(invalidService, "validateFatorKConfiguration"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("sifap.payment.fator-k must be greater than 0 and less than or equal to 1");
    }

    @Test
    void should_update_program_fator_k() {
        UUID programId = UUID.randomUUID();
        ProgramEntity entity = ProgramEntity.withIdAndType(programId, com.sifap.program.domain.ProgramType.ASSISTENCIAL);
        given(programRepository.findById(programId)).willReturn(Optional.of(entity));
        given(programRepository.save(any(ProgramEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        ProgramView view = programService.updateFatorK(programId, new BigDecimal("0.50"));

        assertThat(view.fatorK()).isEqualByComparingTo("0.50");
    }
}