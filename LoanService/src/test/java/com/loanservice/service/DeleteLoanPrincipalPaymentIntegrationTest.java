package com.loanservice.service;

import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeleteLoanPrincipalPaymentIntegration.
 * Based on the Delete operation in the Principal Payment API spec.
 */
@ExtendWith(MockitoExtension.class)
class DeleteLoanPrincipalPaymentIntegrationTest {

    @Mock
    private LoanPrincipalPaymentRepository repository;

    @InjectMocks
    private DeleteLoanPrincipalPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("DeleteLoanPrincipalPaymentIntegration");
        validRequest.setTransaction("LoanPrincipalPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }

    // ---- loanTransactionId validation ----

    @Test
    @DisplayName("loanTransactionId: Required - null should fail validation")
    void loanTransactionId_required_nullShouldFail() {
        // loanTransactionId is required to identify the Loan Principal Payment to delete.
        validRequest.setLoanTransactionId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required - blank should fail validation")
    void loanTransactionId_required_blankShouldFail() {
        validRequest.setLoanTransactionId("");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required - whitespace-only should fail")
    void loanTransactionId_required_whitespaceShouldFail() {
        validRequest.setLoanTransactionId("   ");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Exceeds max length of 50 characters")
    void loanTransactionId_maxLength_exceeded() {
        // loanTransactionId max length is 50 characters.
        validRequest.setLoanTransactionId("X".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid 24-char UUID passes validation")
    void loanTransactionId_valid_24chars() {
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid at max boundary (50 chars)")
    void loanTransactionId_valid_maxBoundary() {
        validRequest.setLoanTransactionId("X".repeat(50));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- basicExecute ----

    @Test
    @DisplayName("basicExecute: deleteByLoanTransactionId called with correct ID")
    void basicExecute_deleteCalledWithCorrectId() {
        // Delete a Loan Principal Payment identified by loanTransactionId.
        doNothing().when(repository).deleteByLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");

        service.basicExecute(validRequest);

        verify(repository, times(1)).deleteByLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }

    @Test
    @DisplayName("basicExecute: Returns success status map")
    void basicExecute_returnsSuccessMap() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) service.basicExecute(validRequest);

        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("basicExecute: Success message contains the loanTransactionId")
    void basicExecute_successMessageContainsId() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) service.basicExecute(validRequest);

        assertTrue(result.get("message").contains("A1B2C3D4E5F6G7H8I9J0K1L2"));
    }

    @Test
    @DisplayName("basicExecute: create() is never called for a DELETE operation")
    void basicExecute_createNeverCalled() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        service.basicExecute(validRequest);

        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("basicExecute: save() is never called for a DELETE operation")
    void basicExecute_saveNeverCalled() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        service.basicExecute(validRequest);

        verify(repository, never()).save(any());
    }

    // ---- full flow: validate then execute ----

    @Test
    @DisplayName("Full flow: valid request validates and deletes successfully")
    void fullFlow_validRequest_deletesSuccessfully() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        assertDoesNotThrow(() -> service.basicValidation(validRequest));

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) service.basicExecute(validRequest);

        assertEquals("SUCCESS", result.get("status"));
        verify(repository, times(1)).deleteByLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }
}
