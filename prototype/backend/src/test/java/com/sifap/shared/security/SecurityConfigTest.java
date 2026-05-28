package com.sifap.shared.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.audit.application.AuditService;
import com.sifap.audit.infrastructure.AuditController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "sifap.security.enabled=true",
        "sifap.security.jwt-secret=test-jwt-secret-at-least-32-bytes"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void should_return_unauthorized_when_no_token_is_provided() throws Exception {
        mockMvc.perform(get("/api/v1/audit-events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_forbidden_for_operator_on_audit_events() throws Exception {
        mockMvc.perform(get("/api/v1/audit-events")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isForbidden());
    }
}