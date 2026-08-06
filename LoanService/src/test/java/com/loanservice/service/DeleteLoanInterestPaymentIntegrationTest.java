package com.loanservice.service;

import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeleteLoanInterestPaymentIntegration.
 * Based on the Delete operation in the LoanInterestPayment API spec.
 */
@ExtendWith(MockitoExtension.class)
class DeleteLoanInterestPaymentIntegrationTest {

    @Mock
    private LoanInterestPaymentRepository repository;

    @InjectMocks
    private DeleteLoanInterestPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("DeleteLoanInterestPaymentIntegration");
        validRequest.setTransaction("LoanInterestPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }

    // ---- loanTransactionId validation ----

    @Test
    @DisplayName("loanTransactionId: Required — null should fail validation")
    void loanTransactionId_required_nullShouldFail() {
        validRequest.setLoanTransactionId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required — blank should fail validation")
    void loanTransactionId_required_blankShouldFail() {
        validRequest.setLoanTransactionId("");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required — whitespace-only should fail")
    void loanTransactionId_required_whitespaceShouldFail() {
        validRequest.setLoanTransactionId("   ");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid 24-char value passes validation")
    void loanTransactionId_valid_24chars() {
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- basicExecute ----

    @Test
    @DisplayName("basicExecute: calls deleteByLoanTransactionId once")
    void basicExecute_callsDeleteByLoanTransactionId() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        service.basicExecute(validRequest);

        verify(repository, times(1)).deleteByLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        verify(repository, never()).create(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("basicExecute: returns SUCCESS status map")
    @SuppressWarnings("unchecked")
    void basicExecute_returnsSuccessMap() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        Object result = service.basicExecute(validRequest);

        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        Map<String, String> resultMap = (Map<String, String>) result;
        assertEquals("SUCCESS", resultMap.get("status"));
        assertTrue(resultMap.get("message").contains("A1B2C3D4E5F6G7H8I9J0K1L2"));
    }

    @Test
    @DisplayName("Full flow: validation passes then delete executes successfully")
    @SuppressWarnings("unchecked")
    void fullFlow_validationAndExecution() {
        doNothing().when(repository).deleteByLoanTransactionId(any());

        assertDoesNotThrow(() -> service.basicValidation(validRequest));

        Object result = service.basicExecute(validRequest);

        Map<String, String> resultMap = (Map<String, String>) result;
        assertEquals("SUCCESS", resultMap.get("status"));
        verify(repository, times(1)).deleteByLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }
}
