package com.sifap.program.infrastructure;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.program.application.ProgramService;
import com.sifap.program.application.ProgramView;
import com.sifap.program.domain.ProgramType;
import com.sifap.shared.infrastructure.ApiExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProgramController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class ProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgramService programService;

    @Test
    void should_list_programs() throws Exception {
        UUID id = UUID.randomUUID();
        given(programService.listPrograms()).willReturn(List.of(new ProgramView(
                id,
                "Programa A",
                ProgramType.ASSISTENCIAL,
                new BigDecimal("1000.00"),
                new BigDecimal("0.100000"),
                new BigDecimal("0.347215"),
                "ACTIVE")));

        mockMvc.perform(get("/api/v1/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].type").value("ASSISTENCIAL"));
    }

    @Test
    void should_update_program_fator_k() throws Exception {
        UUID id = UUID.randomUUID();
        given(programService.updateFatorK(eq(id), eq(new BigDecimal("0.5")))).willReturn(new ProgramView(
                id,
                "Programa A",
                ProgramType.ASSISTENCIAL,
                new BigDecimal("1000.00"),
                new BigDecimal("0.100000"),
                new BigDecimal("0.5"),
                "ACTIVE"));

        mockMvc.perform(put("/api/v1/programs/{programId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fatorK": 0.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fatorK").value(0.5));
    }
}