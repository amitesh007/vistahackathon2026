package com.loanservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoanInterestPayment entity covering all getters/setters
 * and JPA lifecycle callbacks.
 */
class LoanInterestPaymentTest {

    private LoanInterestPayment entity;

    @BeforeEach
    void setUp() {
        entity = new LoanInterestPayment();
    }

    @Test
    @DisplayName("loanTransactionId: getter and setter work correctly")
    void loanTransactionId_getterSetter() {
        entity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", entity.getLoanTransactionId());
    }

    @Test
    @DisplayName("transactionDate: getter and setter work correctly")
    void transactionDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        entity.setTransactionDate(date);
        assertEquals(date, entity.getTransactionDate());
    }

    @Test
    @DisplayName("eventComment: getter and setter work correctly")
    void eventComment_getterSetter() {
        entity.setEventComment("Interest payment event");
        assertEquals("Interest payment event", entity.getEventComment());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: defaults to false")
    void preventOnlineDeletionIndicator_defaultFalse() {
        assertFalse(entity.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: setter persists true")
    void preventOnlineDeletionIndicator_setTrue() {
        entity.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        assertTrue(entity.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("transactionDescription: getter and setter work correctly")
    void transactionDescription_getterSetter() {
        entity.setTransactionDescription("New interest payment");
        assertEquals("New interest payment", entity.getTransactionDescription());
    }

    @Test
    @DisplayName("prorationTypeCode: getter and setter work correctly")
    void prorationTypeCode_getterSetter() {
        entity.setProrationTypeCode("ACTUAL");
        assertEquals("ACTUAL", entity.getProrationTypeCode());
    }

    @Test
    @DisplayName("cycleId: getter and setter work correctly")
    void cycleId_getterSetter() {
        entity.setCycleId("CYCLE001");
        assertEquals("CYCLE001", entity.getCycleId());
    }

    @Test
    @DisplayName("applyToEarliestCycle: defaults to false")
    void applyToEarliestCycle_defaultFalse() {
        assertFalse(entity.getApplyToEarliestCycle());
    }

    @Test
    @DisplayName("applyToEarliestCycle: setter persists true")
    void applyToEarliestCycle_setTrue() {
        entity.setApplyToEarliestCycle(Boolean.TRUE);
        assertTrue(entity.getApplyToEarliestCycle());
    }

    @Test
    @DisplayName("smeSystemSourceId: getter and setter work correctly")
    void smeSystemSourceId_getterSetter() {
        entity.setSmeSystemSourceId("LIQ");
        assertEquals("LIQ", entity.getSmeSystemSourceId());
    }

    @Test
    @DisplayName("sourceRefNum: getter and setter work correctly")
    void sourceRefNum_getterSetter() {
        entity.setSourceRefNum("REF001");
        assertEquals("REF001", entity.getSourceRefNum());
    }

    @Test
    @DisplayName("principalPaymentAmount: getter and setter work correctly")
    void principalPaymentAmount_getterSetter() {
        entity.setPrincipalPaymentAmount("500000.00");
        assertEquals("500000.00", entity.getPrincipalPaymentAmount());
    }

    @Test
    @DisplayName("requestedAmount: getter and setter work correctly")
    void requestedAmount_getterSetter() {
        entity.setRequestedAmount("1000000.00");
        assertEquals("1000000.00", entity.getRequestedAmount());
    }

    @Test
    @DisplayName("effectiveDate: getter and setter work correctly")
    void effectiveDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        entity.setEffectiveDate(date);
        assertEquals(date, entity.getEffectiveDate());
    }

    @Test
    @DisplayName("loanAlias: getter and setter work correctly")
    void loanAlias_getterSetter() {
        entity.setLoanAlias("LOAN_ALIAS_123");
        assertEquals("LOAN_ALIAS_123", entity.getLoanAlias());
    }

    @Test
    @DisplayName("loanId: getter and setter work correctly")
    void loanId_getterSetter() {
        entity.setLoanId("LOAN987654321");
        assertEquals("LOAN987654321", entity.getLoanId());
    }

    @Test
    @DisplayName("cycleStartDate: getter and setter work correctly")
    void cycleStartDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        entity.setCycleStartDate(date);
        assertEquals(date, entity.getCycleStartDate());
    }

    @Test
    @DisplayName("onCreate: sets createTimeStamp and updateTimeStamp")
    void onCreate_setsTimestamps() {
        entity.onCreate();
        assertNotNull(entity.getCreateTimeStamp());
        assertNotNull(entity.getUpdateTimeStamp());
    }

    @Test
    @DisplayName("onUpdate: updates updateTimeStamp")
    void onUpdate_setsUpdateTimestamp() {
        entity.onCreate();
        LocalDateTime createdAt = entity.getCreateTimeStamp();
        entity.onUpdate();
        assertNotNull(entity.getUpdateTimeStamp());
        assertEquals(createdAt, entity.getCreateTimeStamp(),
                "createTimeStamp must not change on update");
    }
}
