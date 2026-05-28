package com.sifap.audit.infrastructure;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.audit.application.AuditEventView;
import com.sifap.audit.application.AuditService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void should_list_all_audit_events_including_exclusao() throws Exception {
        given(auditService.list(null, null, null, null)).willReturn(List.of(
                new AuditEventView(
                        "Payment",
                        UUID.randomUUID(),
                        "EXCLUSAO",
                        "{\"status\":\"APPROVED\"}",
                        "{\"status\":\"CANCELLED\"}",
                        "auditor1",
                        Instant.parse("2026-05-27T18:00:00Z"),
                        "manual review")));

        mockMvc.perform(get("/api/v1/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("EXCLUSAO"));
    }
}