package com.loanservice.controller;

import com.loanservice.model.LoanRequest;
import com.loanservice.service.BaseIntegrationService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes four endpoints for Loan Principal Payment operations.
 *
 * <p>Each endpoint reads the {@code className} field from the request payload and
 * uses it to look up the corresponding Spring-managed {@link BaseIntegrationService}
 * bean.  The bean names are registered with the exact class-name values found in the
 * JSON payloads (e.g. {@code "CreateLoanPrincipalPaymentIntegration"}).
 *
 * <pre>
 *  POST   /api/loan        → CreateLoanPrincipalPaymentIntegration
 *  PUT    /api/loan        → UpdateLoanPrincipalPaymentIntegration
 *  GET    /api/loan        → GetLoanPrincipalPaymentIntegration
 *  DELETE /api/loan        → DeleteLoanPrincipalPaymentIntegration
 * </pre>
 */
@RestController
@RequestMapping("/api/loan")
public class LoanController {

    private final ApplicationContext applicationContext;

    public LoanController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // ------------------------------------------------------------------ POST
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody LoanRequest request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------ PUT
    @PutMapping
    public ResponseEntity<Object> update(@RequestBody LoanRequest request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------ GET
    @GetMapping
    public ResponseEntity<Object> getById(@RequestBody LoanRequest request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------- DELETE
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody LoanRequest request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------- helper
    /**
     * Resolves the service bean by the exact {@code className} string from the
     * payload.  The {@code @Service} beans are registered with those names.
     *
     * @throws IllegalArgumentException if no bean matches the className
     */
    private BaseIntegrationService resolveService(String className) {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("className field is required in the request payload");
        }
        try {
            return (BaseIntegrationService) applicationContext.getBean(className);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "No service found for className: '" + className + "'", ex);
        }
    }
}
