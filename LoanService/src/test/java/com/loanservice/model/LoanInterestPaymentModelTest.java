package com.loanservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoanInterestPaymentModel covering all getters/setters.
 */
class LoanInterestPaymentModelTest {

    private LoanInterestPaymentModel model;

    @BeforeEach
    void setUp() {
        model = new LoanInterestPaymentModel();
    }

    @Test
    @DisplayName("className: getter and setter work correctly")
    void className_getterSetter() {
        model.setClassName("CreateLoanInterestPaymentIntegration");
        assertEquals("CreateLoanInterestPaymentIntegration", model.getClassName());
    }

    @Test
    @DisplayName("transaction: getter and setter work correctly")
    void transaction_getterSetter() {
        model.setTransaction("LoanInterestPayment");
        assertEquals("LoanInterestPayment", model.getTransaction());
    }

    @Test
    @DisplayName("transactionDate: getter and setter work correctly")
    void transactionDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 3, 15);
        model.setTransactionDate(date);
        assertEquals(date, model.getTransactionDate());
    }

    @Test
    @DisplayName("eventComment: getter and setter work correctly")
    void eventComment_getterSetter() {
        model.setEventComment("Interest payment event");
        assertEquals("Interest payment event", model.getEventComment());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: getter and setter work correctly")
    void preventOnlineDeletionIndicator_getterSetter() {
        model.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        assertTrue(model.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: defaults to null when not set")
    void preventOnlineDeletionIndicator_defaultNull() {
        assertNull(model.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("transactionDescription: getter and setter work correctly")
    void transactionDescription_getterSetter() {
        model.setTransactionDescription("Scheduled interest payment");
        assertEquals("Scheduled interest payment", model.getTransactionDescription());
    }

    @Test
    @DisplayName("prorationTypeCode: getter and setter work correctly")
    void prorationTypeCode_getterSetter() {
        model.setProrationTypeCode("ACTUAL");
        assertEquals("ACTUAL", model.getProrationTypeCode());
    }

    @Test
    @DisplayName("cycleId: getter and setter work correctly")
    void cycleId_getterSetter() {
        model.setCycleId("CYCLE001");
        assertEquals("CYCLE001", model.getCycleId());
    }

    @Test
    @DisplayName("applyToEarliestCycle: getter and setter work correctly")
    void applyToEarliestCycle_getterSetter() {
        model.setApplyToEarliestCycle(Boolean.TRUE);
        assertTrue(model.getApplyToEarliestCycle());
    }

    @Test
    @DisplayName("applyToEarliestCycle: defaults to null when not set")
    void applyToEarliestCycle_defaultNull() {
        assertNull(model.getApplyToEarliestCycle());
    }

    @Test
    @DisplayName("smeSystemSourceId: getter and setter work correctly")
    void smeSystemSourceId_getterSetter() {
        model.setSmeSystemSourceId("LIQ");
        assertEquals("LIQ", model.getSmeSystemSourceId());
    }

    @Test
    @DisplayName("sourceRefNum: getter and setter work correctly")
    void sourceRefNum_getterSetter() {
        model.setSourceRefNum("REF001");
        assertEquals("REF001", model.getSourceRefNum());
    }

    @Test
    @DisplayName("principalPaymentAmount: getter and setter work correctly")
    void principalPaymentAmount_getterSetter() {
        model.setPrincipalPaymentAmount("500000.00");
        assertEquals("500000.00", model.getPrincipalPaymentAmount());
    }

    @Test
    @DisplayName("loanTransactionId: getter and setter work correctly")
    void loanTransactionId_getterSetter() {
        model.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", model.getLoanTransactionId());
    }

    @Test
    @DisplayName("requestedAmount: getter and setter work correctly")
    void requestedAmount_getterSetter() {
        model.setRequestedAmount("1000000.00");
        assertEquals("1000000.00", model.getRequestedAmount());
    }

    @Test
    @DisplayName("effectiveDate: getter and setter work correctly")
    void effectiveDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        model.setEffectiveDate(date);
        assertEquals(date, model.getEffectiveDate());
    }

    @Test
    @DisplayName("loanAlias: getter and setter work correctly")
    void loanAlias_getterSetter() {
        model.setLoanAlias("LOAN_ALIAS_123");
        assertEquals("LOAN_ALIAS_123", model.getLoanAlias());
    }

    @Test
    @DisplayName("loanId: getter and setter work correctly")
    void loanId_getterSetter() {
        model.setLoanId("LOAN987654321");
        assertEquals("LOAN987654321", model.getLoanId());
    }

    @Test
    @DisplayName("cycleStartDate: getter and setter work correctly")
    void cycleStartDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        model.setCycleStartDate(date);
        assertEquals(date, model.getCycleStartDate());
    }
}
