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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateLoanPrincipalPaymentIntegration.
 * Each test targets a specific attribute as described in the Principal Payment API spec.
 */
@ExtendWith(MockitoExtension.class)
class CreateLoanPrincipalPaymentIntegrationTest {

    @Mock
    private LoanPrincipalPaymentRepository repository;

    @InjectMocks
    private CreateLoanPrincipalPaymentIntegration service;

    private LoanRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanRequest();
        validRequest.setClassName("CreateLoanPrincipalPaymentIntegration");
        validRequest.setTransaction("LoanPrincipalPayment");
        validRequest.setRequestedAmount("1000000");
        validRequest.setEffectiveDate(LocalDate.of(2026, 1, 1));
        validRequest.setLoanAlias("LoanAlias123");
        validRequest.setLoanId("565665675");
    }

    // ---- requestedAmount ----

    @Test
    @DisplayName("requestedAmount: Required - should throw when null")
    void requestedAmount_required_nullShouldFail() {
        // The requested amount of the outstanding transaction.
        // Validation: Required to save.
        validRequest.setRequestedAmount(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest),
                "requestedAmount is required and must not be blank");
    }

    @Test
    @DisplayName("requestedAmount: Required - should throw when blank")
    void requestedAmount_required_blankShouldFail() {
        validRequest.setRequestedAmount("  ");
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("requestedAmount: Exceeds max length of 30 characters")
    void requestedAmount_maxLength_exceeded() {
        // Validation: Required to save. Cannot be more than the amount of the current loan.
        validRequest.setRequestedAmount("1".repeat(31));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("requestedAmount: Valid value passes validation")
    void requestedAmount_valid() {
        validRequest.setRequestedAmount("9999999.99");
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- effectiveDate ----

    @Test
    @DisplayName("effectiveDate: Required - should throw when null")
    void effectiveDate_required_nullShouldFail() {
        // The effective date of the transaction.
        // Validation: Required to save. Cannot be prior to the Loan Effective Date.
        validRequest.setEffectiveDate(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("effectiveDate: Valid date passes validation")
    void effectiveDate_valid() {
        validRequest.setEffectiveDate(LocalDate.of(2026, 6, 15));
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
        validRequest.setEventComment("A".repeat(256));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("eventComment: Valid value at max boundary (255 chars)")
    void eventComment_maxLength_boundary() {
        validRequest.setEventComment("A".repeat(255));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- preventOnlineDeletionIndicator ----

    @Test
    @DisplayName("preventOnlineDeletionIndicator: Optional - null defaults to false")
    void preventOnlineDeletionIndicator_optional_nullDefaultsFalse() {
        // Prevents the user from deleting the loan. Optional Boolean.
        validRequest.setPreventOnlineDeletionIndicator(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));

        LoanPrincipalPayment saved = new LoanPrincipalPayment();
        when(repository.create(any())).thenReturn(saved);
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("preventOnlineDeletionIndicator: Passed as true (Y)")
    void preventOnlineDeletionIndicator_true() {
        validRequest.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        LoanPrincipalPayment saved = new LoanPrincipalPayment();
        when(repository.create(any())).thenAnswer(inv -> {
            LoanPrincipalPayment e = inv.getArgument(0);
            return e;
        });
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertTrue(result.getPreventOnlineDeletionIndicator());
    }

    // ---- transactionDescription ----

    @Test
    @DisplayName("transactionDescription: Optional - null is allowed")
    void transactionDescription_optional_nullAllowed() {
        // The description of the transaction. Optional field.
        validRequest.setTransactionDescription(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("transactionDescription: Exceeds max length of 255 characters")
    void transactionDescription_maxLength_exceeded() {
        validRequest.setTransactionDescription("T".repeat(256));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    // ---- loanAlias ----

    @Test
    @DisplayName("loanAlias: Conditionally Required - unique client-defined name. Max 104 chars")
    void loanAlias_maxLength_exceeded() {
        // Conditionally Required. Must be unique within Bank.
        validRequest.setLoanAlias("A".repeat(105));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanAlias: Valid value at max boundary (104 chars)")
    void loanAlias_maxLength_boundary() {
        validRequest.setLoanAlias("A".repeat(104));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- loanId ----

    @Test
    @DisplayName("loanId: Conditionally Required - unique identifier. Max 106 chars")
    void loanId_maxLength_exceeded() {
        // Conditionally Required. Either loanAlias or loanId must be present.
        validRequest.setLoanId("X".repeat(107));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("loanId: Valid value at max boundary (106 chars)")
    void loanId_maxLength_boundary() {
        validRequest.setLoanId("X".repeat(106));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- suppressBreakfunding ----

    @Test
    @DisplayName("suppressBreakfunding: Optional Boolean - null defaults to false (N)")
    void suppressBreakfunding_optional_defaultsFalse() {
        validRequest.setSuppressBreakfunding(null);
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getSuppressBreakfunding());
    }

    // ---- transactionDate ----

    @Test
    @DisplayName("transactionDate: Optional - null is allowed")
    void transactionDate_optional_nullAllowed() {
        // The date the institution receives the Borrower's monthly installment.
        // Applicable only when ITDPY system parameter is set to Y.
        validRequest.setTransactionDate(null);
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("transactionDate: Valid date is accepted")
    void transactionDate_validDate() {
        validRequest.setTransactionDate(LocalDate.of(2026, 1, 15));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- systemSourceId ----

    @Test
    @DisplayName("systemSourceId: Optional - allows user to define the Source System. Max 50 chars")
    void systemSourceId_maxLength_exceeded() {
        // Allows user to define the Source System in the Source System ID code table.
        validRequest.setSystemSourceId("S".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("systemSourceId: Valid value at max boundary (50 chars)")
    void systemSourceId_maxLength_boundary() {
        validRequest.setSystemSourceId("S".repeat(50));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- sourceRefNum ----

    @Test
    @DisplayName("sourceRefNum: Optional - defines Source Reference Number at transaction level. Max 50 chars")
    void sourceRefNum_maxLength_exceeded() {
        // Allows the user to define the Source Reference Number at the transaction level.
        validRequest.setSourceRefNum("R".repeat(51));
        assertThrows(IllegalArgumentException.class,
                () -> service.basicValidation(validRequest));
    }

    @Test
    @DisplayName("sourceRefNum: Valid value at max boundary (50 chars)")
    void sourceRefNum_maxLength_boundary() {
        validRequest.setSourceRefNum("R".repeat(50));
        assertDoesNotThrow(() -> service.basicValidation(validRequest));
    }

    // ---- autoReduceFacility ----

    @Test
    @DisplayName("autoReduceFacility: Optional - null defaults to false (N)")
    void autoReduceFacility_optional_defaultsFalse() {
        // Auto Reduce Facility: Indicates whether or not the auto reduce facility is applicable.
        // If Y, facility commitment amount is reduced by the amount of the principal payment.
        validRequest.setAutoReduceFacility(null);
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertFalse(result.getAutoReduceFacility());
    }

    @Test
    @DisplayName("autoReduceFacility: When Y - auto reduce facility is applicable")
    void autoReduceFacility_true() {
        validRequest.setAutoReduceFacility(Boolean.TRUE);
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertTrue(result.getAutoReduceFacility());
    }

    // ---- basicExecute: loanTransactionId auto-generation ----

    @Test
    @DisplayName("basicExecute: loanTransactionId is auto-generated as 24-char UUID")
    void basicExecute_loanTransactionId_autoGenerated() {
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertNotNull(result.getLoanTransactionId());
        assertEquals(24, result.getLoanTransactionId().length());
    }

    @Test
    @DisplayName("basicExecute: transaction type is set from request.getTransaction()")
    void basicExecute_transactionType_setFromRequest() {
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        LoanPrincipalPayment result = (LoanPrincipalPayment) service.basicExecute(validRequest);
        assertEquals("LoanPrincipalPayment", result.getTransactionType());
    }

    @Test
    @DisplayName("basicExecute: repository.create() is called once")
    void basicExecute_repositoryCreateCalledOnce() {
        when(repository.create(any())).thenReturn(new LoanPrincipalPayment());
        service.basicExecute(validRequest);
        verify(repository, times(1)).create(any(LoanPrincipalPayment.class));
    }
}
