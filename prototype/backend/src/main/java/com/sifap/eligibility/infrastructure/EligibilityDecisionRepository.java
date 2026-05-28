package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.domain.EligibilityDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EligibilityDecisionRepository extends JpaRepository<EligibilityDecisionEntity, UUID> {
}