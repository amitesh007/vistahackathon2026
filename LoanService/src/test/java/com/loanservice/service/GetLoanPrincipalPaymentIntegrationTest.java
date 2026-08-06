package com.loanservice.service;

import com.loanservice.entity.LoanPrincipalPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetLoanPrincipalPaymentIntegration.
 * Based on the GetByID operation in the Principal Payment API spec.
 */
@ExtendWith(MockitoExtension.class)
class GetLoanPrincipalPaymentIntegrationTest {

    @Mock
    private LoanPrincipalPaymentRepository repository;

    @InjectMocks
    private GetLoanPrincipalPaymentIntegration service;

    private LoanRequest validRequest;
    private LoanPrincipalPayment existingEntity;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("GetLoanPrincipalPaymentIntegration");
        validRequest.setTransaction("LoanPrincipalPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");

        existingEntity = new LoanPrincipalPayment();
        existingEntity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        existingEntity.setRequestedAmount("1000000");
        existingEntity.setEffectiveDate(LocalDate.of(2026, 1, 1));
        existingEntity.setTransactionType("LoanPrincipalPayment");
    }

    // ---- loanTransactionId validation ----

    @Test
    @DisplayName("loanTransactionId: Required - null should fail validation")
    void loanTransactionId_required_nullShouldFail() {
        // loanTransactionId is required to identify the specific Loan Principal Payment record.
        validRequest.setLoanTransactionId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required - blank should fail validation")
    void loanTransactionId_required_blankShouldFail() {
        validRequest.setLoanTransactionId("  ");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Exceeds max length of 50 characters")
    void loanTransactionId_maxLength_exceeded() {
        validRequest.setLoanTransactionId("X".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid 24-char value passes validation")
    void loanTransactionId_valid_24chars() {
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid max boundary (50 chars)")
    void loanTransactionId_valid_maxBoundary() {
        validRequest.setLoanTransactionId("X".repeat(50));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- basicExecute: record found ----

    @Test
    @DisplayName("basicExecute: Returns entity when loanTransactionId is found")
    void basicExecute_recordFound_returnsEntity() {
        // Get details of specific Loan Principal Payment by its transaction ID.
        when(repository.findById(validRequest.getLoanTransactionId()))
                .thenReturn(Optional.of(existingEntity));

        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);

        assertNotNull(result);
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", result.getLoanTransactionId());
        assertEquals("1000000", result.getRequestedAmount());
        assertEquals(LocalDate.of(2026, 1, 1), result.getEffectiveDate());
    }

    @Test
    @DisplayName("basicExecute: Returns correct transactionType from persisted entity")
    void basicExecute_returnsCorrectTransactionType() {
        when(repository.findById(validRequest.getLoanTransactionId()))
                .thenReturn(Optional.of(existingEntity));

        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);

        assertEquals("LoanPrincipalPayment", result.getTransactionType());
    }

    // ---- basicExecute: record not found ----

    @Test
    @DisplayName("basicExecute: Throws 404 when loanTransactionId is not found")
    void basicExecute_recordNotFound_throws404() {
        // If the transaction ID does not exist, should return NOT_FOUND.
        when(repository.findById(validRequest.getLoanTransactionId()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.basicExecute(validRequest));
    }

    @Test
    @DisplayName("basicExecute: 404 error message contains the loanTransactionId")
    void basicExecute_recordNotFound_errorMessageContainsId() {
        when(repository.findById(validRequest.getLoanTransactionId()))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.basicExecute(validRequest));

        assertTrue(ex.getReason().contains("A1B2C3D4E5F6G7H8I9J0K1L2"));
    }

    // ---- repository interaction ----

    @Test
    @DisplayName("basicExecute: findById called exactly once with the correct loanTransactionId")
    void basicExecute_findByIdCalledOnce() {
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.of(existingEntity));

        service.basicExecute(validRequest);

        verify(repository, times(1)).findById("A1B2C3D4E5F6G7H8I9J0K1L2");
    }

    @Test
    @DisplayName("basicExecute: create() is never called for a GET operation")
    void basicExecute_createNeverCalled() {
        when(repository.findById(any())).thenReturn(Optional.of(existingEntity));

        service.basicExecute(validRequest);

        verify(repository, never()).create(any());
    }
}
