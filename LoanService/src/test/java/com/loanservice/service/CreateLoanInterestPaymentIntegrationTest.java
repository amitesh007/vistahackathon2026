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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateLoanInterestPaymentIntegration.
 * Based on the Create operation in the LoanInterestPayment API spec.
 */
@ExtendWith(MockitoExtension.class)
class CreateLoanInterestPaymentIntegrationTest {

    @Mock
    private LoanInterestPaymentRepository repository;

    @InjectMocks
    private CreateLoanInterestPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("CreateLoanInterestPaymentIntegration");
        validRequest.setTransaction("LoanInterestPayment");
    }

    // ---- basicValidation (all fields are optional) ----

    @Test
    @DisplayName("basicValidation: empty request passes — all Create fields are optional")
    void basicValidation_emptyRequest_passes() {
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("basicValidation: request with transactionDate passes")
    void basicValidation_withTransactionDate_passes() {
        validRequest.setTransactionDate(LocalDate.of(2026, 6, 1));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("basicValidation: request with all Create fields passes")
    void basicValidation_allCreateFields_passes() {
        validRequest.setTransactionDate(LocalDate.of(2026, 6, 1));
        validRequest.setEventComment("Interest payment");
        validRequest.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        validRequest.setTransactionDescription("Scheduled interest payment");
        validRequest.setProrationTypeCode("ACTUAL");
        validRequest.setCycleId("CYCLE001");
        validRequest.setApplyToEarliestCycle(Boolean.FALSE);
        validRequest.setSmeSystemSourceId("LIQ");
        validRequest.setSourceRefNum("REF001");
        validRequest.setPrincipalPaymentAmount("500000.00");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- basicExecute ----

    @Test
    @DisplayName("basicExecute: persists entity via repository.create()")
    void basicExecute_callsRepositoryCreate() {
        LoanInterestPayment savedEntity = new LoanInterestPayment();
        savedEntity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        when(repository.create(any(LoanInterestPayment.class))).thenReturn(savedEntity);

        Object result = service.basicExecute(validRequest);

        assertNotNull(result);
        verify(repository, times(1)).create(any(LoanInterestPayment.class));
        verify(repository, never()).save(any());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("basicExecute: preventOnlineDeletionIndicator defaults to false when null")
    void basicExecute_preventOnlineDeletionIndicator_defaultsFalse() {
        validRequest.setPreventOnlineDeletionIndicator(null);
        LoanInterestPayment savedEntity = new LoanInterestPayment();
        when(repository.create(any(LoanInterestPayment.class))).thenAnswer(inv -> {
            LoanInterestPayment e = inv.getArgument(0);
            assertFalse(e.getPreventOnlineDeletionIndicator(),
                    "preventOnlineDeletionIndicator should default to false");
            return e;
        });
        service.basicExecute(validRequest);
        verify(repository, times(1)).create(any(LoanInterestPayment.class));
    }

    @Test
    @DisplayName("basicExecute: applyToEarliestCycle defaults to false when null")
    void basicExecute_applyToEarliestCycle_defaultsFalse() {
        validRequest.setApplyToEarliestCycle(null);
        LoanInterestPayment savedEntity = new LoanInterestPayment();
        when(repository.create(any(LoanInterestPayment.class))).thenAnswer(inv -> {
            LoanInterestPayment e = inv.getArgument(0);
            assertFalse(e.getApplyToEarliestCycle(),
                    "applyToEarliestCycle should default to false");
            return e;
        });
        service.basicExecute(validRequest);
        verify(repository, times(1)).create(any(LoanInterestPayment.class));
    }

    @Test
    @DisplayName("basicExecute: maps all Create fields to entity correctly")
    void basicExecute_mapsAllCreateFieldsToEntity() {
        validRequest.setTransactionDate(LocalDate.of(2026, 6, 1));
        validRequest.setEventComment("Interest event");
        validRequest.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        validRequest.setTransactionDescription("Scheduled payment");
        validRequest.setProrationTypeCode("ACTUAL");
        validRequest.setCycleId("CYCLE001");
        validRequest.setApplyToEarliestCycle(Boolean.TRUE);
        validRequest.setSmeSystemSourceId("LIQ");
        validRequest.setSourceRefNum("REF001");
        validRequest.setPrincipalPaymentAmount("500000.00");

        when(repository.create(any(LoanInterestPayment.class))).thenAnswer(inv -> {
            LoanInterestPayment e = inv.getArgument(0);
            assertNotNull(e.getLoanTransactionId(), "loanTransactionId must be auto-generated");
            assertEquals(LocalDate.of(2026, 6, 1), e.getTransactionDate());
            assertEquals("Interest event", e.getEventComment());
            assertTrue(e.getPreventOnlineDeletionIndicator());
            assertEquals("Scheduled payment", e.getTransactionDescription());
            assertEquals("ACTUAL", e.getProrationTypeCode());
            assertEquals("CYCLE001", e.getCycleId());
            assertTrue(e.getApplyToEarliestCycle());
            assertEquals("LIQ", e.getSmeSystemSourceId());
            assertEquals("REF001", e.getSourceRefNum());
            assertEquals("500000.00", e.getPrincipalPaymentAmount());
            return e;
        });

        service.basicExecute(validRequest);
        verify(repository, times(1)).create(any(LoanInterestPayment.class));
    }
}
