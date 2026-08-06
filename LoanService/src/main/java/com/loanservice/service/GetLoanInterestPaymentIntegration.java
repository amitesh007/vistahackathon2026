package com.loanservice.service;

import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for the GetById operation on LoanInterestPayment.
 * Bean name matches the {@code className} value: "GetLoanInterestPaymentIntegration".
 */
@Service("GetLoanInterestPaymentIntegration")
public class GetLoanInterestPaymentIntegration extends BaseIntegrationService {

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
     * Retrieves the LoanInterestPayment identified by {@code loanTransactionId}.
     * Throws {@link ResponseStatusException} with HTTP 404 when not found.
     * Never calls repository.create() or repository.save().
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        return repository.findById(request.getLoanTransactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "LoanInterestPayment not found for loanTransactionId: "
                                + request.getLoanTransactionId()));
    }
}
