package com.sifap.program.infrastructure;

import com.sifap.program.domain.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgramRepository extends JpaRepository<ProgramEntity, UUID> {
}