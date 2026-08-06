package com.loanservice.repository;

import com.loanservice.entity.LoanPrincipalPayment;

/**
 * Custom repository fragment that exposes a semantically named create() method
 * used by service implementations via basicExecute().
 */
public interface LoanPrincipalPaymentRepositoryCustom {

    LoanPrincipalPayment create(LoanPrincipalPayment entity);
}
