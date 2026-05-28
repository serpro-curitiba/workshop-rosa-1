package com.sifap.beneficiary.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialCpfPrefixRepository extends JpaRepository<SpecialCpfPrefixEntity, String> {
}