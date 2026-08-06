package com.loanservice.repository;

import com.loanservice.entity.LoanInterestPayment;

/**
 * Custom repository fragment that exposes a semantically named create() method
 * used by service implementations via basicExecute().
 */
public interface LoanInterestPaymentRepositoryCustom {

    LoanInterestPayment create(LoanInterestPayment entity);
}
