package com.sifap.payment.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sifap.payment.application.DuplicatePaymentException;
import com.sifap.payment.application.PaymentStatusTransitionException;
import com.sifap.payment.application.PaymentCycleService;
import com.sifap.payment.application.PaymentView;
import com.sifap.shared.domain.PaymentStatus;
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

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentCycleService paymentCycleService;

    @Test
    void should_create_payment_cycle() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID beneficiaryId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        given(paymentCycleService.generatePayment(any())).willReturn(new PaymentView(
                paymentId,
                beneficiaryId,
                programId,
                "202612",
                new BigDecimal("2150.00"),
                new BigDecimal("1850.00"),
                PaymentStatus.PENDING));

        mockMvc.perform(post("/api/v1/payment-cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "beneficiaryId": "%s",
                                  "programId": "%s",
                                  "referenceYearMonth": "202612",
                                  "baseValue": 1000.00,
                                  "regionalFactor": 1.0,
                                  "familyFactor": 1.0,
                                  "incomeFactor": 1.0,
                                  "ageFactor": 1.0,
                                  "programType": "A",
                                  "discounts": [
                                    { "type": "OUTROS", "amount": 300.00 }
                                  ]
                                }
                                """.formatted(beneficiaryId, programId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.grossAmount").value(2150.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void should_return_conflict_when_duplicate_payment_exists() throws Exception {
        given(paymentCycleService.generatePayment(any()))
                .willThrow(new DuplicatePaymentException("Payment already exists for beneficiary and reference month"));

        mockMvc.perform(post("/api/v1/payment-cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "beneficiaryId": "%s",
                                  "programId": "%s",
                                  "referenceYearMonth": "202612",
                                  "baseValue": 1000.00,
                                  "regionalFactor": 1.0,
                                  "familyFactor": 1.0,
                                  "incomeFactor": 1.0,
                                  "ageFactor": 1.0,
                                  "programType": "A",
                                  "discounts": []
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Payment already exists for beneficiary and reference month"));
    }

    @Test
    void should_list_payments_for_reference_month() throws Exception {
        given(paymentCycleService.listCycle(eq("202605"))).willReturn(List.of(
                new PaymentView(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "202605",
                        new BigDecimal("1000.00"),
                        new BigDecimal("800.00"),
                        PaymentStatus.PENDING)));

        mockMvc.perform(get("/api/v1/payment-cycles/202605"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].referenceYearMonth").value("202605"))
                .andExpect(jsonPath("$[0].netAmount").value(800.00));

        verify(paymentCycleService).listCycle("202605");
    }

    @Test
    void should_approve_payment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(paymentCycleService.approvePayment(eq(paymentId))).willReturn(new PaymentView(
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202605",
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                PaymentStatus.APPROVED));

        mockMvc.perform(patch("/api/v1/payments/{paymentId}/approve", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void should_reject_payment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(paymentCycleService.rejectPayment(eq(paymentId))).willReturn(new PaymentView(
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "202605",
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                PaymentStatus.REJECTED));

        mockMvc.perform(patch("/api/v1/payments/{paymentId}/reject", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void should_return_conflict_when_payment_state_transition_is_invalid() throws Exception {
        UUID paymentId = UUID.randomUUID();
        given(paymentCycleService.approvePayment(eq(paymentId)))
                .willThrow(new PaymentStatusTransitionException("Payment status transition is invalid"));

        mockMvc.perform(patch("/api/v1/payments/{paymentId}/approve", paymentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Payment status transition is invalid"));
    }
}