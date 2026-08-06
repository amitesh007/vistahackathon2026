package com.loanservice.service;

import com.loanservice.entity.LoanInterestPayment;
import com.loanservice.model.LoanRequest;
import com.loanservice.repository.LoanInterestPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for the Update operation on LoanInterestPayment.
 * Bean name matches the {@code className} value: "UpdateLoanInterestPaymentIntegration".
 */
@Service("UpdateLoanInterestPaymentIntegration")
public class UpdateLoanInterestPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanInterestPaymentRepository repository;

    /**
     * Validates required fields for an Update (LoanInterestPayment) request.
     * {@code loanTransactionId} is the mandatory identifier.
     */
    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("loanTransactionId", request.getLoanTransactionId());
    }

    /**
     * Locates an existing LoanInterestPayment by {@code loanTransactionId},
     * applies updates to UPDATABLE fields, then persists via
     * {@code repository.save()} for existing records or {@code repository.create()} for new ones.
     */
    @Override
    public Object basicExecute(LoanRequest request) {
        Optional<LoanInterestPayment> existing =
                repository.findById(request.getLoanTransactionId());
        LoanInterestPayment entity = existing.orElse(new LoanInterestPayment());

        entity.setLoanTransactionId(request.getLoanTransactionId());

        // Patch UPDATABLE=Y fields
        entity.setRequestedAmount(request.getRequestedAmount());
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setProrationTypeCode(request.getProrationTypeCode());
        entity.setLoanAlias(request.getLoanAlias());
        entity.setSourceRefNum(request.getSourceRefNum());

        if (existing.isPresent()) {
            return repository.save(entity);
        }
        return repository.create(entity);
    }
}
