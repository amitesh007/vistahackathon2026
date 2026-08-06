package com.loanservice.service;

import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for the Delete operation.
 * Bean name matches the {@code className} value: "DeleteLoanPrincipalPaymentIntegration".
 */
@Service("DeleteLoanPrincipalPaymentIntegration")
public class DeleteLoanPrincipalPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanPrincipalPaymentRepository repository;

    /**
     * Validates that the loanTransactionId identifier is present.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("loanTransactionId", request.getLoanTransactionId());
        assertMaxLength("loanTransactionId", request.getLoanTransactionId(), 50);
    }

    /**
     * Deletes the LoanPrincipalPayment identified by {@code loanTransactionId}.
     */
    @Override
    @Transactional
    public Object basicExecute(LoanRequest request) {
        repository.deleteByLoanTransactionId(request.getLoanTransactionId());
        return Map.of(
                "status",  "SUCCESS",
                "message", "LoanPrincipalPayment deleted for loanTransactionId: "
                           + request.getLoanTransactionId()
        );
    }
}
