package com.loanservice.service;

import com.loanservice.entity.LoanInterestPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
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
 * Unit tests for GetLoanInterestPaymentIntegration.
 * Based on the GetByID operation in the LoanInterestPayment API spec.
 */
@ExtendWith(MockitoExtension.class)
class GetLoanInterestPaymentIntegrationTest {

    @Mock
    private LoanInterestPaymentRepository repository;

    @InjectMocks
    private GetLoanInterestPaymentIntegration service;

    private LoanRequest validRequest;
    private LoanInterestPayment existingEntity;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("GetLoanInterestPaymentIntegration");
        validRequest.setTransaction("LoanInterestPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");

        existingEntity = new LoanInterestPayment();
        existingEntity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        existingEntity.setRequestedAmount("1000000.00");
        existingEntity.setEffectiveDate(LocalDate.of(2026, 6, 1));
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
        validRequest.setLoanTransactionId("  ");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Valid value passes validation")
    void loanTransactionId_valid() {
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- basicExecute: record found ----

    @Test
    @DisplayName("basicExecute: record found — returns the entity")
    void basicExecute_recordFound_returnsEntity() {
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.of(existingEntity));

        Object result = service.basicExecute(validRequest);

        assertNotNull(result);
        assertInstanceOf(LoanInterestPayment.class, result);
        LoanInterestPayment found = (LoanInterestPayment) result;
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", found.getLoanTransactionId());
        assertEquals("1000000.00", found.getRequestedAmount());
        verify(repository, times(1)).findById("A1B2C3D4E5F6G7H8I9J0K1L2");
        verify(repository, never()).create(any());
        verify(repository, never()).save(any());
    }

    // ---- basicExecute: record not found ----

    @Test
    @DisplayName("basicExecute: record not found — throws ResponseStatusException NOT_FOUND")
    void basicExecute_recordNotFound_throwsResponseStatusException() {
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.basicExecute(validRequest));

        verify(repository, times(1)).findById("A1B2C3D4E5F6G7H8I9J0K1L2");
        verify(repository, never()).create(any());
        verify(repository, never()).save(any());
    }
}
