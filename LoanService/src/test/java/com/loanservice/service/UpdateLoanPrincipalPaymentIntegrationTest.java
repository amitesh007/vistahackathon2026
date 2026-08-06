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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateLoanPrincipalPaymentIntegration.
 * Each test targets a specific attribute as described in the Principal Payment API spec.
 */
@ExtendWith(MockitoExtension.class)
class UpdateLoanPrincipalPaymentIntegrationTest {

    @Mock
    private LoanPrincipalPaymentRepository repository;

    @InjectMocks
    private UpdateLoanPrincipalPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("UpdateLoanPrincipalPaymentIntegration");
        validRequest.setTransaction("LoanPrincipalPayment");
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J10K11");
        validRequest.setRequestedAmount("7000000");
        validRequest.setEffectiveDate(LocalDate.of(2026, 1, 1));
    }

    // ---- loanTransactionId ----

    @Test
    @DisplayName("loanTransactionId: Required - Record ID of the Principal Payment to be updated")
    void loanTransactionId_required_nullShouldFail() {
        // This is Record ID of Principal Payment transaction intended to be updated.
        // The RID and Effective date information will identify the transaction.
        validRequest.setLoanTransactionId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanTransactionId: Required - blank should fail")
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
    @DisplayName("loanTransactionId: Valid value passes validation")
    void loanTransactionId_valid() {
        validRequest.setLoanTransactionId("A1B2C3D4E5F6G7H8I9J0K1L2");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- requestedAmount ----

    @Test
    @DisplayName("requestedAmount: Required for update - modifying creates an event")
    void requestedAmount_required_nullShouldFail() {
        // The requested amount of the outstanding transaction.
        // Modifying the Requested amount creates an event.
        validRequest.setRequestedAmount(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("requestedAmount: Exceeds max length of 30 characters")
    void requestedAmount_maxLength_exceeded() {
        validRequest.setRequestedAmount("9".repeat(31));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("requestedAmount: Valid value at max boundary (30 chars)")
    void requestedAmount_maxLength_boundary() {
        validRequest.setRequestedAmount("9".repeat(30));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- effectiveDate ----

    @Test
    @DisplayName("effectiveDate: Required - null should fail")
    void effectiveDate_required_nullShouldFail() {
        // For a Loan, the transaction effective date cannot be prior to the Loan Effective Date,
        // Facility Effective Date, or Borrower Effective Date.
        validRequest.setEffectiveDate(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("effectiveDate: Valid future date accepted")
    void effectiveDate_validFutureDate() {
        validRequest.setEffectiveDate(LocalDate.of(2027, 12, 31));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- eventComment ----

    @Test
    @DisplayName("eventComment: Optional - null is allowed")
    void eventComment_optional_nullAllowed() {
        // A description of the event. Optional field.
        validRequest.setEventComment(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("eventComment: Exceeds max length of 255 characters")
    void eventComment_maxLength_exceeded() {
        validRequest.setEventComment("E".repeat(256));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- preventOnlineDeletionIndicator ----

    @Test
    @DisplayName("preventOnlineDeletionIndicator: Optional Boolean - null defaults to false (N)")
    void preventOnlineDeletionIndicator_optional_defaultsFalse() {
        // Prevents the user from deleting the loan. Optional Boolean.
        validRequest.setPreventOnlineDeletionIndicator(null);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getPreventOnlineDeletionIndicator());
    }

    // ---- transactionDescription ----

    @Test
    @DisplayName("transactionDescription: Optional - null allowed. Max 255 chars")
    void transactionDescription_maxLength_exceeded() {
        // The description of the transaction.
        validRequest.setTransactionDescription("D".repeat(256));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- loanAlias ----

    @Test
    @DisplayName("loanAlias: Information only field for update - ignored in processing. Max 104 chars")
    void loanAlias_maxLength_exceeded() {
        // The unique client-defined name for the outstanding.
        // This is an information only field and will be ignored in the Inputs here.
        validRequest.setLoanAlias("A".repeat(105));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- loanId ----

    @Test
    @DisplayName("loanId: Information only field for update - ignored in processing. Max 106 chars")
    void loanId_maxLength_exceeded() {
        // The unique identifier for the outstanding.
        // This is an information only field and will be ignored in the Inputs here.
        validRequest.setLoanId("X".repeat(107));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- suppressBreakfunding ----

    @Test
    @DisplayName("suppressBreakfunding: Optional Boolean - null defaults to false (N)")
    void suppressBreakfunding_optional_defaultsFalse() {
        validRequest.setSuppressBreakfunding(null);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getSuppressBreakfunding());
    }

    // ---- transactionDate ----

    @Test
    @DisplayName("transactionDate: Optional - date institution receives monthly installment")
    void transactionDate_optional_nullAllowed() {
        // The date the institution that services the loan receives the Borrower's monthly installment.
        // Applicable only when ITDPY system parameter is set to Y.
        validRequest.setTransactionDate(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- systemSourceId ----

    @Test
    @DisplayName("systemSourceId: Optional - identifies the calling system. Max 50 chars")
    void systemSourceId_maxLength_exceeded() {
        // This field identifies the system that is calling the REST API.
        // Allows user to define the Source System in the Source System ID code table.
        validRequest.setSystemSourceId("S".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- sourceRefNum ----

    @Test
    @DisplayName("sourceRefNum: Optional - Source Reference Number at transaction level. Max 50 chars")
    void sourceRefNum_maxLength_exceeded() {
        // Allows the user to define the Source Reference Number at the transaction level.
        validRequest.setSourceRefNum("R".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- applyToEarliestItem ----

    @Test
    @DisplayName("applyToEarliestItem: Optional Boolean - null defaults to false (N)")
    void applyToEarliestItem_optional_defaultsFalse() {
        validRequest.setApplyToEarliestItem(null);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getApplyToEarliestItem());
    }

    @Test
    @DisplayName("applyToEarliestItem: When true (Y) - value is persisted")
    void applyToEarliestItem_true() {
        validRequest.setApplyToEarliestItem(Boolean.TRUE);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertTrue(result.getApplyToEarliestItem());
    }

    // ---- scheduleDate ----

    @Test
    @DisplayName("scheduleDate: Optional - null is allowed")
    void scheduleDate_optional_nullAllowed() {
        validRequest.setScheduleDate(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("scheduleDate: Valid date is accepted")
    void scheduleDate_validDate() {
        validRequest.setScheduleDate(LocalDate.of(2026, 6, 30));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- autoReduceFacility ----

    @Test
    @DisplayName("autoReduceFacility: Optional - if Y, facility commitment reduced by principal amount")
    void autoReduceFacility_optional_defaultsFalse() {
        // If Y is passed, the facility commitment amount is reduced by the amount of the principal payment.
        validRequest.setAutoReduceFacility(null);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getAutoReduceFacility());
    }

    // ---- basicExecute: existing record update ----

    @Test
    @DisplayName("basicExecute: Existing record is updated via save() not create()")
    void basicExecute_existingRecord_usedSave() {
        LoanPrincipalPayment existing = new LoanPrincipalPayment();
        existing.setLoanTransactionId(validRequest.getLoanTransactionId());
        when(repository.findById(validRequest.getLoanTransactionId()))
                .thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        service.basicExecute(validRequest);

        verify(repository, times(1)).save(any());
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("basicExecute: New record (not found) uses create()")
    void basicExecute_newRecord_usesCreate() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.create(any())).thenReturn(new LoanPrincipalPayment());

        service.basicExecute(validRequest);

        verify(repository, times(1)).create(any());
        verify(repository, never()).save(any());
    }
}
