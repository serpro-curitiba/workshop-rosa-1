package com.sifap.eligibility.infrastructure;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.eligibility.application.EligibilityDecisionView;
import com.sifap.eligibility.application.EligibilityEvaluationService;
import com.sifap.eligibility.domain.EligibilityDecisionStatus;
import com.sifap.shared.infrastructure.ApiExceptionHandler;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EligibilityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class EligibilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EligibilityEvaluationService eligibilityEvaluationService;

    @Test
    void should_evaluate_eligibility() throws Exception {
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        given(eligibilityEvaluationService.evaluate(eq(beneficiaryId), eq(programId)))
                .willReturn(new EligibilityDecisionView(
                        beneficiaryId,
                        programId,
                        EligibilityDecisionStatus.APPROVED,
                        "REGIAO_ESPECIAL_99"));

        mockMvc.perform(post("/api/v1/eligibility/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "beneficiaryId": "%s",
                                  "programId": "%s"
                                }
                                """.formatted(beneficiaryId, programId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.reason").value("REGIAO_ESPECIAL_99"));
    }
}