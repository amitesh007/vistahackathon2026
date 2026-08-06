package com.loanservice.service;

import com.loanservice.entity.LoanInterestPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
import com.loanservice.util.TransactionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for the Create operation on LoanInterestPayment.
 * Bean name matches the {@code className} value: "CreateLoanInterestPaymentIntegration".
 */
@Service("CreateLoanInterestPaymentIntegration")
public class CreateLoanInterestPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanInterestPaymentRepository repository;

    /**
     * Validates constraints for a Create (LoanInterestPayment) request.
     * All Create fields are optional with no defined max-length constraints.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        // All Create fields are optional — no mandatory field or max-length constraints defined
    }

    /**
     * Creates a new LoanInterestPayment transaction and persists it via
     * {@code repository.create()}.
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        LoanInterestPayment entity = new LoanInterestPayment();

        // Auto-generate a 24-character unique transaction ID
        entity.setLoanTransactionId(TransactionIdGenerator.generate());

        entity.setTransactionDate(request.getTransactionDate());
        entity.setEventComment(request.getEventComment());
        entity.setPreventOnlineDeletionIndicator(
                request.getPreventOnlineDeletionIndicator() != null
                        ? request.getPreventOnlineDeletionIndicator() : Boolean.FALSE);
        entity.setTransactionDescription(request.getTransactionDescription());
        entity.setProrationTypeCode(request.getProrationTypeCode());
        entity.setCycleId(request.getCycleId());
        entity.setApplyToEarliestCycle(
                request.getApplyToEarliestCycle() != null
                        ? request.getApplyToEarliestCycle() : Boolean.FALSE);
        entity.setSmeSystemSourceId(request.getSmeSystemSourceId());
        entity.setSourceRefNum(request.getSourceRefNum());
        entity.setPrincipalPaymentAmount(request.getPrincipalPaymentAmount());

        return repository.create(entity);
    }
}
