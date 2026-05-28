package com.sifap.beneficiary.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.beneficiary.application.BeneficiaryService;
import com.sifap.beneficiary.application.BeneficiaryView;
import com.sifap.beneficiary.domain.BeneficiaryNotFoundException;
import com.sifap.beneficiary.domain.BeneficiaryStatus;
import com.sifap.beneficiary.domain.InvalidCpfException;
import com.sifap.shared.infrastructure.ApiExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(BeneficiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class BeneficiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BeneficiaryService beneficiaryService;

    @Test
    void should_create_beneficiary() throws Exception {
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryService.create(any())).willReturn(new BeneficiaryView(
                beneficiaryId,
                "52998224725",
                "Maria",
                LocalDate.parse("1948-01-01"),
                BeneficiaryStatus.SUSPENDED,
                1,
                3,
                new BigDecimal("400.00"),
                List.of()));

        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "529.982.247-25",
                                  "name": "Maria",
                                  "birthDate": "1948-01-01",
                                  "status": "ACTIVE",
                                  "codRegion": 1,
                                  "familyMembers": 3,
                                  "familyIncome": 400.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(beneficiaryId.toString()))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void should_return_bad_request_for_invalid_cpf() throws Exception {
        given(beneficiaryService.create(any())).willThrow(new InvalidCpfException("CPF check digits are invalid"));

        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "111.111.111-11",
                                  "name": "Maria",
                                  "birthDate": "1980-01-01",
                                  "status": "ACTIVE",
                                  "codRegion": 1,
                                  "familyMembers": 3,
                                  "familyIncome": 400.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("CPF check digits are invalid"));
    }

    @Test
    void should_get_beneficiary_by_id() throws Exception {
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryService.getById(eq(beneficiaryId))).willReturn(new BeneficiaryView(
                beneficiaryId,
                "52998224725",
                "Maria",
                LocalDate.parse("1980-01-01"),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00"),
                List.of()));

        mockMvc.perform(get("/api/v1/beneficiaries/{beneficiaryId}", beneficiaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("52998224725"));
    }

    @Test
    void should_return_not_found_when_beneficiary_does_not_exist() throws Exception {
        UUID beneficiaryId = UUID.randomUUID();
        given(beneficiaryService.getById(eq(beneficiaryId))).willThrow(new BeneficiaryNotFoundException("Beneficiary not found"));

        mockMvc.perform(get("/api/v1/beneficiaries/{beneficiaryId}", beneficiaryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Beneficiary not found"));
    }

    @Test
    void should_add_dependent() throws Exception {
        UUID beneficiaryId = UUID.randomUUID();
        UUID dependentId = UUID.randomUUID();
        given(beneficiaryService.addDependent(eq(beneficiaryId), any())).willReturn(new BeneficiaryView(
                beneficiaryId,
                "52998224725",
                "Maria",
                LocalDate.parse("1980-01-01"),
                BeneficiaryStatus.ACTIVE,
                1,
                3,
                new BigDecimal("400.00"),
                List.of(new BeneficiaryView.DependentView(
                        dependentId,
                        "Joao",
                        LocalDate.parse("2015-05-10"),
                        "SON"))));

        mockMvc.perform(post("/api/v1/beneficiaries/{beneficiaryId}/dependents", beneficiaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao",
                                  "birthDate": "2015-05-10",
                                  "relationship": "SON"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dependents[0].id").value(dependentId.toString()));

        verify(beneficiaryService).addDependent(eq(beneficiaryId), any());
    }
}