package com.loanservice.repository;

import com.loanservice.entity.LoanInterestPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Primary Spring Data JPA repository for LoanInterestPayment.
 * Inherits create() from LoanInterestPaymentRepositoryCustom.
 */
@Repository
public interface LoanInterestPaymentRepository
        extends JpaRepository<LoanInterestPayment, String>, LoanInterestPaymentRepositoryCustom {

    void deleteByLoanTransactionId(String loanTransactionId);
}
