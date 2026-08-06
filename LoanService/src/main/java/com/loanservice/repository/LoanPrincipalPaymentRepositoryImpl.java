package com.loanservice.repository;

import com.loanservice.entity.LoanPrincipalPayment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the custom create() fragment.
 * Spring Data JPA picks this up automatically because the class name follows
 * the '<RepositoryInterface>Impl' convention.
 */
@Repository
public class LoanPrincipalPaymentRepositoryImpl implements LoanPrincipalPaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public LoanPrincipalPayment create(LoanPrincipalPayment entity) {
        entityManager.persist(entity);
        return entity;
    }
}
