package com.loanservice.service;

import com.loanservice.model.LoanRequest;

/**
 * Abstract base class for all Integration service classes.
 *
 * <p>Each concrete service is named exactly after the {@code className} value
 * in the incoming JSON payload, allowing the controller to dispatch dynamically
 * using Spring's ApplicationContext.
 *
 * <ul>
 *   <li>{@link #basicValidation(LoanRequest)} – validates attribute lengths
 *   <li>{@link #basicExecute(LoanRequest)}    – creates/processes the transaction
 *       defined by {@code request.getTransaction()} and persists it via the repository
 * </ul>
 */
public abstract class BaseIntegrationService {

    /**
     * Validates the length of all relevant string attributes in the request.
     * Throws {@link IllegalArgumentException} if any field violates its constraint.
     *
     * @param request the incoming loan request payload
     */
    public abstract void basicValidation(LoanRequest request);

    /**
     * Executes the core transaction logic identified by {@code request.getTransaction()}.
     * Internally calls {@code repository.create()} to persist the entity.
     *
     * @param request the incoming loan request payload
     * @return the persisted or retrieved entity / response object
     */
    public abstract Object basicExecute(LoanRequest request);

    // ---- Shared validation helpers ----

    protected void assertMaxLength(String fieldName, String value, int maxLen) {
        if (value != null && value.length() > maxLen) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' exceeds maximum allowed length of %d (actual: %d)",
                            fieldName, maxLen, value.length()));
        }
    }

    protected void assertNotBlank(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is required and must not be blank", fieldName));
        }
    }
}
