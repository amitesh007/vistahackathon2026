package com.loanservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoanPrincipalPayment entity covering all getters/setters
 * and JPA lifecycle callbacks.
 */
class LoanPrincipalPaymentTest {

    private LoanPrincipalPayment entity;

    @BeforeEach
    void setUp() {
        entity = new LoanPrincipalPayment();
    }

    @Test
    @DisplayName("loanTransactionId: getter and setter work correctly")
    void loanTransactionId_getterSetter() {
        entity.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertEquals("A1B2C3D4E5F6G7H8I9J0K1L2", entity.getLoanTransactionId());
    }

    @Test
    @DisplayName("transactionType: getter and setter work correctly")
    void transactionType_getterSetter() {
        entity.setTransactionType("LoanPrincipalPayment");
        assertEquals("LoanPrincipalPayment", entity.getTransactionType());
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
        LocalDate date = LocalDate.of(2026, 1, 1);
        entity.setEffectiveDate(date);
        assertEquals(date, entity.getEffectiveDate());
    }

    @Test
    @DisplayName("eventComment: getter and setter work correctly")
    void eventComment_getterSetter() {
        entity.setEventComment("Principal payment event");
        assertEquals("Principal payment event", entity.getEventComment());
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
        entity.setTransactionDescription("New principal payment");
        assertEquals("New principal payment", entity.getTransactionDescription());
    }

    @Test
    @DisplayName("loanAlias: getter and setter work correctly")
    void loanAlias_getterSetter() {
        entity.setLoanAlias("LoanAlias123");
        assertEquals("LoanAlias123", entity.getLoanAlias());
    }

    @Test
    @DisplayName("loanId: getter and setter work correctly")
    void loanId_getterSetter() {
        entity.setLoanId("565665675");
        assertEquals("565665675", entity.getLoanId());
    }

    @Test
    @DisplayName("suppressBreakfunding: defaults to false")
    void suppressBreakfunding_defaultFalse() {
        assertFalse(entity.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("suppressBreakfunding: setter persists true")
    void suppressBreakfunding_setTrue() {
        entity.setSuppressBreakfunding(Boolean.TRUE);
        assertTrue(entity.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("transactionDate: getter and setter work correctly")
    void transactionDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 3, 15);
        entity.setTransactionDate(date);
        assertEquals(date, entity.getTransactionDate());
    }

    @Test
    @DisplayName("systemSourceId: getter and setter work correctly")
    void systemSourceId_getterSetter() {
        entity.setSystemSourceId("LIQ");
        assertEquals("LIQ", entity.getSystemSourceId());
    }

    @Test
    @DisplayName("sourceRefNum: getter and setter work correctly")
    void sourceRefNum_getterSetter() {
        entity.setSourceRefNum("REF-1234");
        assertEquals("REF-1234", entity.getSourceRefNum());
    }

    @Test
    @DisplayName("autoReduceFacility: defaults to false")
    void autoReduceFacility_defaultFalse() {
        assertFalse(entity.getAutoReduceFacility());
    }

    @Test
    @DisplayName("autoReduceFacility: setter persists true")
    void autoReduceFacility_setTrue() {
        entity.setAutoReduceFacility(Boolean.TRUE);
        assertTrue(entity.getAutoReduceFacility());
    }

    @Test
    @DisplayName("applyToEarliestItem: defaults to false")
    void applyToEarliestItem_defaultFalse() {
        assertFalse(entity.getApplyToEarliestItem());
    }

    @Test
    @DisplayName("applyToEarliestItem: setter persists true")
    void applyToEarliestItem_setTrue() {
        entity.setApplyToEarliestItem(Boolean.TRUE);
        assertTrue(entity.getApplyToEarliestItem());
    }

    @Test
    @DisplayName("scheduleDate: getter and setter work correctly")
    void scheduleDate_getterSetter() {
        LocalDate date = LocalDate.of(2026, 6, 30);
        entity.setScheduleDate(date);
        assertEquals(date, entity.getScheduleDate());
    }

    @Test
    @DisplayName("onCreate: sets createTimeStamp and updateTimeStamp before persist")
    void onCreate_setsTimestamps() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        entity.onCreate();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(entity.getCreateTimeStamp());
        assertNotNull(entity.getUpdateTimeStamp());
        assertTrue(entity.getCreateTimeStamp().isAfter(before));
        assertTrue(entity.getCreateTimeStamp().isBefore(after));
        assertTrue(entity.getUpdateTimeStamp().isAfter(before));
    }

    @Test
    @DisplayName("onUpdate: updates updateTimeStamp on entity update")
    void onUpdate_updatesUpdateTimestamp() {
        entity.onCreate();
        LocalDateTime afterCreate = entity.getUpdateTimeStamp();

        entity.onUpdate();

        assertNotNull(entity.getUpdateTimeStamp());
        // updateTimeStamp should be >= the value after create
        assertFalse(entity.getUpdateTimeStamp().isBefore(afterCreate));
    }

    @Test
    @DisplayName("onUpdate: createTimeStamp is not changed on update")
    void onUpdate_doesNotChangeCreateTimestamp() {
        entity.onCreate();
        LocalDateTime createTs = entity.getCreateTimeStamp();

        entity.onUpdate();

        assertEquals(createTs, entity.getCreateTimeStamp());
    }

    @Test
    @DisplayName("All null values: entity fields remain null after construction")
    void newEntity_allFieldsNullExceptBooleans() {
        assertNull(entity.getLoanTransactionId());
        assertNull(entity.getTransactionType());
        assertNull(entity.getRequestedAmount());
        assertNull(entity.getEffectiveDate());
        assertNull(entity.getEventComment());
        assertNull(entity.getTransactionDescription());
        assertNull(entity.getLoanAlias());
        assertNull(entity.getLoanId());
        assertNull(entity.getTransactionDate());
        assertNull(entity.getSystemSourceId());
        assertNull(entity.getSourceRefNum());
        assertNull(entity.getScheduleDate());
        assertNull(entity.getCreateTimeStamp());
        assertNull(entity.getUpdateTimeStamp());
        // Booleans default to false
        assertFalse(entity.getPreventOnlineDeletionIndicator());
        assertFalse(entity.getSuppressBreakfunding());
        assertFalse(entity.getAutoReduceFacility());
        assertFalse(entity.getApplyToEarliestItem());
    }
}
