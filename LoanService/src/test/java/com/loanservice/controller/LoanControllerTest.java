package com.loanservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.loanservice.entity.LoanPrincipalPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import com.loanservice.service.BaseIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-style tests for LoanController using full Spring context.
 * Service beans are replaced with mocks so no real DB is hit.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "CreateLoanPrincipalPaymentIntegration")
    private BaseIntegrationService createService;

    @MockBean(name = "UpdateLoanPrincipalPaymentIntegration")
    private BaseIntegrationService updateService;

    @MockBean(name = "GetLoanPrincipalPaymentIntegration")
    private BaseIntegrationService getService;

    @MockBean(name = "DeleteLoanPrincipalPaymentIntegration")
    private BaseIntegrationService deleteService;

    @MockBean
    private LoanPrincipalPaymentRepository loanRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private LoanRequest buildCreateRequest() {
        LoanRequest req = new LoanRequest();
        req.setClassName("CreateLoanPrincipalPaymentIntegration");
        req.setTransaction("LoanPrincipalPayment");
        req.setRequestedAmount("1000000");
        req.setEffectiveDate(LocalDate.of(2026, 1, 1));
        req.setLoanAlias("LoanAlias123");
        req.setLoanId("565665675");
        return req;
    }

    private LoanRequest buildUpdateRequest() {
        LoanRequest req = new LoanRequest();
        req.setClassName("UpdateLoanPrincipalPaymentIntegration");
        req.setTransaction("LoanPrincipalPayment");
        req.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        req.setRequestedAmount("7000000");
        req.setEffectiveDate(LocalDate.of(2026, 1, 1));
        return req;
    }

    private LoanRequest buildGetByIdRequest() {
        LoanRequest req = new LoanRequest();
        req.setClassName("GetLoanPrincipalPaymentIntegration");
        req.setTransaction("LoanPrincipalPayment");
        req.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        return req;
    }

    private LoanRequest buildDeleteRequest() {
        LoanRequest req = new LoanRequest();
        req.setClassName("DeleteLoanPrincipalPaymentIntegration");
        req.setTransaction("LoanPrincipalPayment");
        req.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        return req;
    }

    // ---- POST /api/loan ----

    @Test
    @DisplayName("POST /api/loan: Returns 200 and result from service")
    void post_create_returns200() throws Exception {
        LoanPrincipalPayment saved = new LoanPrincipalPayment();
        saved.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        doNothing().when(createService).basicValidation(any());
        when(createService.basicExecute(any())).thenReturn(saved);

        mockMvc.perform(post("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isOk());

        verify(createService).basicValidation(any());
        verify(createService).basicExecute(any());
    }

    @Test
    @DisplayName("POST /api/loan: className null returns 400")
    void post_create_noClassName_returns400() throws Exception {
        LoanRequest req = buildCreateRequest();
        req.setClassName(null);
        mockMvc.perform(post("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/loan: Unknown className returns 400")
    void post_create_unknownClassName_returns400() throws Exception {
        LoanRequest req = buildCreateRequest();
        req.setClassName("NonExistentService");
        mockMvc.perform(post("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/loan: Validation failure returns 400")
    void post_create_validationFailure_returns400() throws Exception {
        doThrow(new IllegalArgumentException("requestedAmount is required"))
                .when(createService).basicValidation(any());
        mockMvc.perform(post("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isBadRequest());
    }

    // ---- PUT /api/loan ----

    @Test
    @DisplayName("PUT /api/loan: Returns 200 and updated entity")
    void put_update_returns200() throws Exception {
        LoanPrincipalPayment updated = new LoanPrincipalPayment();
        updated.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        doNothing().when(updateService).basicValidation(any());
        when(updateService.basicExecute(any())).thenReturn(updated);

        mockMvc.perform(put("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/loan: Validation failure returns 400")
    void put_update_validationFailure_returns400() throws Exception {
        doThrow(new IllegalArgumentException("loanTransactionId is required"))
                .when(updateService).basicValidation(any());
        mockMvc.perform(put("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest())))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /api/loan ----

    @Test
    @DisplayName("GET /api/loan: Returns 200 with found entity")
    void get_getById_returns200() throws Exception {
        LoanPrincipalPayment found = new LoanPrincipalPayment();
        found.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        doNothing().when(getService).basicValidation(any());
        when(getService.basicExecute(any())).thenReturn(found);

        mockMvc.perform(get("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildGetByIdRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/loan: className blank returns 400")
    void get_getById_blankClassName_returns400() throws Exception {
        LoanRequest req = buildGetByIdRequest();
        req.setClassName("   ");
        mockMvc.perform(get("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/loan ----

    @Test
    @DisplayName("DELETE /api/loan: Returns 200 with success message")
    void delete_returns200() throws Exception {
        doNothing().when(deleteService).basicValidation(any());
        when(deleteService.basicExecute(any()))
                .thenReturn(Map.of("status", "SUCCESS", "message", "Deleted"));

        mockMvc.perform(delete("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDeleteRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/loan: Validation failure returns 400")
    void delete_validationFailure_returns400() throws Exception {
        doThrow(new IllegalArgumentException("loanTransactionId is required"))
                .when(deleteService).basicValidation(any());
        mockMvc.perform(delete("/api/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDeleteRequest())))
                .andExpect(status().isBadRequest());
    }
}
