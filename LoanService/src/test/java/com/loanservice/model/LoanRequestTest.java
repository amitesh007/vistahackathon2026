package com.loanservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoanRequest covering all getters/setters.
 */
class LoanRequestTest {

    private LoanRequest request;

    @BeforeEach
    void setUp() {
        request = new LoanRequest();
    }

    @Test
    @DisplayName("className: getter and setter work correctly")
    void className_getterSetter() {
        request.setClassName("CreateLoanPrincipalPaymentIntegration");
        assertEquals("CreateLoanPrincipalPaymentIntegration", request.getClassName());
    }

    @Test
    @DisplayName("transaction: getter and setter work correctly")
    void transaction_getterSetter() {
        request.setTransaction("LoanPrincipalPayment");
        assertEquals("LoanPrincipalPayment", request.getTransaction());
    }

    @Test
    @DisplayName("requestedAmount: getter and setter work correctly")
    void requestedAmount_getterSetter() {
        request.setRequestedAmount("1000000");
        assertEquals("1000000", request.getRequestedAmount());
    }

    @Test
    @DisplayName("effectiveDate: getter and setter work correctly")
    void effectiveDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        request.setEffectiveDate(date);
        assertEquals(date, request.getEffectiveDate());
    }

    @Test
    @DisplayName("eventComment: getter and setter work correctly")
    void eventComment_getterSetter() {
        request.setEventComment("Principal payment event");
        assertEquals("Principal payment event", request.getEventComment());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: getter and setter work correctly")
    void preventOnlineDeletionIndicator_getterSetter() {
        request.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        assertTrue(request.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: null is allowed")
    void preventOnlineDeletionIndicator_null() {
        request.setPreventOnlineDeletionIndicator(null);
        assertNull(request.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("transactionDescription: getter and setter work correctly")
    void transactionDescription_getterSetter() {
        request.setTransactionDescription("New Principal Payment");
        assertEquals("New Principal Payment", request.getTransactionDescription());
    }

    @Test
    @DisplayName("loanAlias: getter and setter work correctly")
    void loanAlias_getterSetter() {
        request.setLoanAlias("LoanAlias123");
        assertEquals("LoanAlias123", request.getLoanAlias());
    }

    @Test
    @DisplayName("loanId: getter and setter work correctly")
    void loanId_getterSetter() {
        request.setLoanId("565665675");
        assertEquals("565665675", request.getLoanId());
    }

    @Test
    @DisplayName("suppressBreakfunding: getter and setter work correctly")
    void suppressBreakfunding_getterSetter() {
        request.setSuppressBreakfunding(Boolean.FALSE);
        assertFalse(request.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("transactionDate: getter and setter work correctly")
    void transactionDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 3, 15);
        request.setTransactionDate(date);
        assertEquals(date, request.getTransactionDate());
    }

    @Test
    @DisplayName("systemSourceId: getter and setter work correctly")
    void systemSourceId_getterSetter() {
        request.setSystemSourceId("LIQ");
        assertEquals("LIQ", request.getSystemSourceId());
    }

    @Test
    @DisplayName("sourceRefNum: getter and setter work correctly")
    void sourceRefNum_getterSetter() {
        request.setSourceRefNum("REF-5678");
        assertEquals("REF-5678", request.getSourceRefNum());
    }

    @Test
    @DisplayName("autoReduceFacility: getter and setter work correctly")
    void autoReduceFacility_getterSetter() {
        request.setAutoReduceFacility(Boolean.TRUE);
        assertTrue(request.getAutoReduceFacility());
    }

    @Test
    @DisplayName("loanTransactionId: getter and setter work correctly")
    void loanTransactionId_getterSetter() {
        request.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", request.getLoanTransactionId());
    }

    @Test
    @DisplayName("applyToEarliestItem: getter and setter work correctly")
    void applyToEarliestItem_getterSetter() {
        request.setApplyToEarliestItem(Boolean.TRUE);
        assertTrue(request.getApplyToEarliestItem());
    }

    @Test
    @DisplayName("scheduleDate: getter and setter work correctly")
    void scheduleDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 6, 30);
        request.setScheduleDate(date);
        assertEquals(date, request.getScheduleDate());
    }

    @Test
    @DisplayName("All fields: new instance has all nulls")
    void newInstance_allFieldsNull() {
        assertNull(request.getClassName());
        assertNull(request.getTransaction());
        assertNull(request.getRequestedAmount());
        assertNull(request.getEffectiveDate());
        assertNull(request.getEventComment());
        assertNull(request.getPreventOnlineDeletionIndicator());
        assertNull(request.getTransactionDescription());
        assertNull(request.getLoanAlias());
        assertNull(request.getLoanId());
        assertNull(request.getSuppressBreakfunding());
        assertNull(request.getTransactionDate());
        assertNull(request.getSystemSourceId());
        assertNull(request.getSourceRefNum());
        assertNull(request.getAutoReduceFacility());
        assertNull(request.getLoanTransactionId());
        assertNull(request.getApplyToEarliestItem());
        assertNull(request.getScheduleDate());
    }
}
