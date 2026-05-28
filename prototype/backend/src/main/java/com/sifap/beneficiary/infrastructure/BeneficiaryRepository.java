package com.sifap.beneficiary.infrastructure;

import com.sifap.beneficiary.domain.BeneficiaryEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, UUID> {

    boolean existsByCpf(String cpf);

    @EntityGraph(attributePaths = "dependents")
    Optional<BeneficiaryEntity> findWithDependentsById(UUID id);
}