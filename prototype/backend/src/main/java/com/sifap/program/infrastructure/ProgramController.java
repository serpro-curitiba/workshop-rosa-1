package com.sifap.program.infrastructure;

import com.sifap.program.application.ProgramService;
import com.sifap.program.application.ProgramView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs")
@Tag(name = "program")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    @Operation(summary = "List all programs")
    public List<ProgramView> listPrograms() {
        return programService.listPrograms();
    }

    @PutMapping("/{programId}")
    @Operation(summary = "Update program fator_k")
    public ProgramView updateProgram(
            @PathVariable UUID programId,
            @Valid @RequestBody UpdateProgramRequest request) {
        return programService.updateFatorK(programId, request.fatorK());
    }

    public record UpdateProgramRequest(
            @NotNull @DecimalMin(value = "0.000001") @DecimalMax(value = "1.0") BigDecimal fatorK) {
    }
}