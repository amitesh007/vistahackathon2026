package com.loanservice.service;

import com.loanservice.entity.LoanPrincipalPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the Update operation.
 * Bean name matches the {@code className} value: "UpdateLoanPrincipalPaymentIntegration".
 */
@Service("UpdateLoanPrincipalPaymentIntegration")
public class UpdateLoanPrincipalPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanPrincipalPaymentRepository repository;

    /**
     * Validates required fields and max-length constraints for an Update request.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("loanTransactionId", request.getLoanTransactionId());
        assertNotBlank("requestedAmount",   request.getRequestedAmount());
        if (request.getEffectiveDate() == null) {
            throw new IllegalArgumentException("Field 'effectiveDate' is required and must not be null");
        }

        assertMaxLength("loanTransactionId",              request.getLoanTransactionId(),            50);
        assertMaxLength("requestedAmount",                request.getRequestedAmount(),              30);
        assertMaxLength("eventComment",                   request.getEventComment(),                255);
        assertMaxLength("transactionDescription",         request.getTransactionDescription(),      255);
        assertMaxLength("loanAlias",                      request.getLoanAlias(),                   104);
        assertMaxLength("loanId",                         request.getLoanId(),                      106);
        assertMaxLength("systemSourceId",                 request.getSystemSourceId(),               50);
        assertMaxLength("sourceRefNum",                   request.getSourceRefNum(),                 50);
    }

    /**
     * Locates an existing LoanPrincipalPayment by {@code loanTransactionId},
     * applies the updates, then persists via {@code repository.create()}.
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        java.util.Optional<LoanPrincipalPayment> existing =
                repository.findById(request.getLoanTransactionId());
        LoanPrincipalPayment entity = existing.orElse(new LoanPrincipalPayment());

        entity.setTransactionType(request.getTransaction());
        entity.setLoanTransactionId(request.getLoanTransactionId());
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
        entity.setApplyToEarliestItem(
                request.getApplyToEarliestItem() != null ? request.getApplyToEarliestItem() : Boolean.FALSE);
        entity.setScheduleDate(request.getScheduleDate());
        entity.setAutoReduceFacility(
                request.getAutoReduceFacility() != null ? request.getAutoReduceFacility() : Boolean.FALSE);

        // Existing records use save(); brand-new records use create()
        if (existing.isPresent()) {
            return repository.save(entity);
        }
        return repository.create(entity);
    }
}
