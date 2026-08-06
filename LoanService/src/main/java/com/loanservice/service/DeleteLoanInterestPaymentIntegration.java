package com.loanservice.service;

import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for the Delete operation on LoanInterestPayment.
 * Bean name matches the {@code className} value: "DeleteLoanInterestPaymentIntegration".
 */
@Service("DeleteLoanInterestPaymentIntegration")
public class DeleteLoanInterestPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanInterestPaymentRepository repository;

    /**
     * Validates that the mandatory identifier is present.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("loanTransactionId", request.getLoanTransactionId());
    }

    /**
     * Deletes the LoanInterestPayment identified by {@code loanTransactionId}.
     * Operation is idempotent — no NOT_FOUND exception is thrown.
     */
    @Override
    @Transactional
    public Object basicExecute(LoanRequest request) {
        repository.deleteByLoanTransactionId(request.getLoanTransactionId());
        return Map.of(
                "status",  "SUCCESS",
                "message", "LoanInterestPayment deleted for loanTransactionId: "
                           + request.getLoanTransactionId()
        );
    }
}
