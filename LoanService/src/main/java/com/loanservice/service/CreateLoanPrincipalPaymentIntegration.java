package com.loanservice.service;

import com.loanservice.entity.LoanPrincipalPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import com.loanservice.util.TransactionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the Create operation.
 * Bean name matches the {@code className} value: "CreateLoanPrincipalPaymentIntegration".
 */
@Service("CreateLoanPrincipalPaymentIntegration")
public class CreateLoanPrincipalPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanPrincipalPaymentRepository repository;

    /**
     * Validates required fields and enforces max-length constraints for a
     * Create (LoanPrincipalPayment) request.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("requestedAmount", request.getRequestedAmount());
        if (request.getEffectiveDate() == null) {
            throw new IllegalArgumentException("Field 'effectiveDate' is required and must not be null");
        }

        assertMaxLength("requestedAmount",              request.getRequestedAmount(),              30);
        assertMaxLength("eventComment",                 request.getEventComment(),                255);
        assertMaxLength("transactionDescription",       request.getTransactionDescription(),      255);
        assertMaxLength("loanAlias",                    request.getLoanAlias(),                   104);
        assertMaxLength("loanId",                       request.getLoanId(),                      106);
        assertMaxLength("systemSourceId",               request.getSystemSourceId(),               50);
        assertMaxLength("sourceRefNum",                 request.getSourceRefNum(),                 50);
    }

    /**
     * Creates a new LoanPrincipalPayment transaction and persists it via
     * {@code repository.create()}.
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();

        // Auto-generate a 24-character unique transaction ID
        entity.setLoanTransactionId(TransactionIdGenerator.generate());

        // Transaction type comes from the 'transaction' field in the payload
        entity.setTransactionType(request.getTransaction());
        entity.setRequestedAmount(request.getRequestedAmount());
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setEventComment(request.getEventComment());
        entity.setPreventOnlineDeletionIndicator(
                request.getPreventOnlineDeletionIndicator() != null ? request.getPreventOnlineDeletionIndicator() : Boolean.FALSE);
        entity.setTransactionDescription(request.getTransactionDescription());
        entity.setLoanAlias(request.getLoanAlias());
        entity.setLoanId(request.getLoanId());
        entity.setSuppressBreakfunding(
                request.getSuppressBreakfunding() != null ? request.getSuppressBreakfunding() : Boolean.FALSE);
        entity.setTransactionDate(request.getTransactionDate());
        entity.setSystemSourceId(request.getSystemSourceId());
        entity.setSourceRefNum(request.getSourceRefNum());
        entity.setAutoReduceFacility(
                request.getAutoReduceFacility() != null ? request.getAutoReduceFacility() : Boolean.FALSE);

        return repository.create(entity);
    }
}
