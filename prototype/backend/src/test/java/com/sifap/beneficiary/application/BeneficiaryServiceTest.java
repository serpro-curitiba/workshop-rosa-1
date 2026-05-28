package com.sifap.beneficiary.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import com.sifap.beneficiary.domain.BeneficiaryStatus;
import com.sifap.beneficiary.domain.DependentLimitReachedException;
import com.sifap.beneficiary.domain.DependentNotAllowedException;
import com.sifap.beneficiary.domain.InvalidCpfException;
import com.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import com.sifap.beneficiary.infrastructure.SpecialCpfPrefixRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private SpecialCpfPrefixRepository specialCpfPrefixRepository;

    @InjectMocks
    private BeneficiaryService beneficiaryService;

    @Test
    void should_suspend_beneficiary_older_than_seventy_five_when_creating() {
        LocalDate birthDate = LocalDate.now().minusYears(76);
        given(beneficiaryRepository.existsByCpf("52998224725")).willReturn(false);
        given(beneficiaryRepository.save(any(BeneficiaryEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        BeneficiaryView view = beneficiaryService.create(new CreateBeneficiaryCommand(
                "529.982.247-25",
                "Maria",
                birthDate,
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00")));

        assertThat(view.status()).isEqualTo(BeneficiaryStatus.SUSPENDED);
    }

    @Test
    void should_reject_invalid_cpf_when_creating() {
        assertThatThrownBy(() -> beneficiaryService.create(new CreateBeneficiaryCommand(
                "111.111.111-11",
                "Maria",
                LocalDate.now().minusYears(30),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00"))))
                .isInstanceOf(InvalidCpfException.class);
    }

    @Test
    void should_allow_fifth_dependent_for_active_beneficiary() {
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(30),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00"));
        for (int index = 0; index < 4; index++) {
            beneficiary.addDependent(com.sifap.beneficiary.domain.DependentEntity.create(
                    "Dep" + index,
                    LocalDate.now().minusYears(10),
                    "SON"));
        }
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryRepository.findWithDependentsById(beneficiaryId)).willReturn(Optional.of(beneficiary));
        given(beneficiaryRepository.save(any(BeneficiaryEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        BeneficiaryView view = beneficiaryService.addDependent(beneficiaryId,
                new AddDependentCommand("Novo", LocalDate.now().minusYears(8), "DAUGHTER"));

        assertThat(view.dependents()).hasSize(5);
    }

    @Test
    void should_reject_sixth_dependent() {
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(30),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00"));
        for (int index = 0; index < 5; index++) {
            beneficiary.addDependent(com.sifap.beneficiary.domain.DependentEntity.create(
                    "Dep" + index,
                    LocalDate.now().minusYears(10),
                    "SON"));
        }
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryRepository.findWithDependentsById(beneficiaryId)).willReturn(Optional.of(beneficiary));

        assertThatThrownBy(() -> beneficiaryService.addDependent(beneficiaryId,
                new AddDependentCommand("Extra", LocalDate.now().minusYears(8), "DAUGHTER")))
                .isInstanceOf(DependentLimitReachedException.class)
                .hasMessage("Limite de dependentes atingido");
    }

    @Test
    void should_reject_dependent_for_cancelled_beneficiary() {
        BeneficiaryEntity beneficiary = BeneficiaryEntity.create(
                "52998224725",
                "Maria",
                LocalDate.now().minusYears(30),
                BeneficiaryStatus.CANCELLED,
                1,
                3,
                new BigDecimal("400.00"));
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryRepository.findWithDependentsById(beneficiaryId)).willReturn(Optional.of(beneficiary));

        assertThatThrownBy(() -> beneficiaryService.addDependent(beneficiaryId,
                new AddDependentCommand("Novo", LocalDate.now().minusYears(8), "DAUGHTER")))
                .isInstanceOf(DependentNotAllowedException.class);
    }
}