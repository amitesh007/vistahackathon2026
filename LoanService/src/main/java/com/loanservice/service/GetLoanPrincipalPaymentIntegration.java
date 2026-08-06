package com.loanservice.service;

import com.loanservice.entity.LoanPrincipalPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanPrincipalPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for the GetById operation.
 * Bean name matches the {@code className} value: "GetLoanPrincipalPaymentIntegration".
 */
@Service("GetLoanPrincipalPaymentIntegration")
public class GetLoanPrincipalPaymentIntegration extends BaseIntegrationService {

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
     * Retrieves the LoanPrincipalPayment identified by {@code loanTransactionId}.
     * The create() call is not invoked for reads; repository.findByLoanTransactionId
     * is used instead.
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        return repository.findById(request.getLoanTransactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "LoanPrincipalPayment not found for loanTransactionId: "
                                + request.getLoanTransactionId()));
    }
}
