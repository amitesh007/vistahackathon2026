package com.loanservice.repository;

import com.loanservice.entity.LoanPrincipalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Primary Spring Data JPA repository for LoanPrincipalPayment.
 * Inherits create() from LoanPrincipalPaymentRepositoryCustom.
 */
@Repository
public interface LoanPrincipalPaymentRepository
        extends JpaRepository<LoanPrincipalPayment, String>, LoanPrincipalPaymentRepositoryCustom {

    void deleteByLoanTransactionId(String loanTransactionId);
}
