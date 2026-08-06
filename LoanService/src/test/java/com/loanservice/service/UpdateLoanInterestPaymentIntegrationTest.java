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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateLoanInterestPaymentIntegration.
 * Based on the Update operation in the LoanInterestPayment API spec.
 */
@ExtendWith(MockitoExtension.class)
class UpdateLoanInterestPaymentIntegrationTest {

    @Mock
    private LoanInterestPaymentRepository repository;

    @InjectMocks
    private UpdateLoanInterestPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("UpdateLoanInterestPaymentIntegration");
        validRequest.setTransaction("LoanInterestPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
    }

    // ---- loanTransactionId ----

    @Test
    @DisplayName("loanTransactionId: Required identifier — null should fail")
    void loanTransactionId_required_nullShouldFail() {
        validRequest.setLoanTransactionId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required identifier — blank should fail")
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

    // ---- basicExecute: existing record ----

    @Test
    @DisplayName("basicExecute: existing record — patches updatable fields and calls save()")
    void basicExecute_existingRecord_callsSave() {
        LoanInterestPayment existingEntity = new LoanInterestPayment();
        existingEntity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.of(existingEntity));
        when(repository.save(any(LoanInterestPayment.class))).thenReturn(existingEntity);

        validRequest.setRequestedAmount("2000000.00");
        validRequest.setEffectiveDate(LocalDate.of(2026, 6, 1));
        validRequest.setProrationTypeCode("ACTUAL");
        validRequest.setLoanAlias("ALIAS_NEW");
        validRequest.setSourceRefNum("REF002");

        Object result = service.basicExecute(validRequest);

        assertNotNull(result);
        verify(repository, times(1)).findById("A1B2C3D4E5F6G7H8I9J0K1L2");
        verify(repository, times(1)).save(any(LoanInterestPayment.class));
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("basicExecute: new record — calls create() when not found")
    void basicExecute_newRecord_callsCreate() {
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.empty());
        LoanInterestPayment newEntity = new LoanInterestPayment();
        when(repository.create(any(LoanInterestPayment.class))).thenReturn(newEntity);

        Object result = service.basicExecute(validRequest);

        assertNotNull(result);
        verify(repository, times(1)).findById("A1B2C3D4E5F6G7H8I9J0K1L2");
        verify(repository, times(1)).create(any(LoanInterestPayment.class));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("basicExecute: patches requestedAmount and effectiveDate on entity")
    void basicExecute_patchesUpdatableFields() {
        LoanInterestPayment existingEntity = new LoanInterestPayment();
        existingEntity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        when(repository.findById("A1B2C3D4E5F6G7H8I9J0K1L2"))
                .thenReturn(Optional.of(existingEntity));
        when(repository.save(any(LoanInterestPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        validRequest.setRequestedAmount("9999999.99");
        validRequest.setEffectiveDate(LocalDate.of(2026, 12, 31));
        validRequest.setProrationTypeCode("30/360");
        validRequest.setLoanAlias("UPDATED_ALIAS");
        validRequest.setSourceRefNum("REF_UPDATED");

        LoanInterestPayment result = (LoanInterestPayment) service.basicExecute(validRequest);

        assertEquals("9999999.99", result.getRequestedAmount());
        assertEquals(LocalDate.of(2026, 12, 31), result.getEffectiveDate());
        assertEquals("30/360", result.getProrationTypeCode());
        assertEquals("UPDATED_ALIAS", result.getLoanAlias());
        assertEquals("REF_UPDATED", result.getSourceRefNum());
    }
}
