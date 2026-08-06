package com.loanservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loanservice.model.YNBooleanSerializer;

/**
 * JPA entity that backs the LOAN_INTEREST_PAYMENT table.
 * Covers every field that may be submitted across create / update / query / delete operations.
 */
@Entity
@Table(name = "LOAN_INTEREST_PAYMENT")
public class LoanInterestPayment {

    @Id
    @Column(name = "LOAN_TRANSACTION_ID", length = 24)
    private String loanTransactionId;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "EVENT_COMMENT")
    private String eventComment;

    @Column(name = "PREVENT_ONLINE_DELETION_INDICATOR")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean preventOnlineDeletionIndicator = Boolean.FALSE;

    @Column(name = "TRANSACTION_DESCRIPTION")
    private String transactionDescription;

    @Column(name = "PRORATION_TYPE_CODE")
    private String prorationTypeCode;

    @Column(name = "CYCLE_ID")
    private String cycleId;

    @Column(name = "APPLY_TO_EARLIEST_CYCLE")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean applyToEarliestCycle = Boolean.FALSE;

    @Column(name = "SME_SYSTEM_SOURCE_ID")
    private String smeSystemSourceId;

    @Column(name = "SOURCE_REF_NUM")
    private String sourceRefNum;

    @Column(name = "PRINCIPAL_PAYMENT_AMOUNT")
    private String principalPaymentAmount;

    @Column(name = "REQUESTED_AMOUNT")
    private String requestedAmount;

    @Column(name = "EFFECTIVE_DATE")
    private LocalDate effectiveDate;

    @Column(name = "LOAN_ALIAS")
    private String loanAlias;

    @Column(name = "LOAN_ID")
    private String loanId;

    @Column(name = "CYCLE_START_DATE")
    private LocalDate cycleStartDate;

    @Column(name = "CREATE_TIMESTAMP", updatable = false)
    private LocalDateTime createTimeStamp;

    @Column(name = "UPDATE_TIMESTAMP")
    private LocalDateTime updateTimeStamp;

    @PrePersist
    protected void onCreate() {
        createTimeStamp = LocalDateTime.now();
        updateTimeStamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTimeStamp = LocalDateTime.now();
    }

    // ---- Getters & Setters ----

    public String getLoanTransactionId() { return loanTransactionId; }
    public void setLoanTransactionId(String loanTransactionId) { this.loanTransactionId = loanTransactionId; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getEventComment() { return eventComment; }
    public void setEventComment(String eventComment) { this.eventComment = eventComment; }

    public Boolean getPreventOnlineDeletionIndicator() { return preventOnlineDeletionIndicator; }
    public void setPreventOnlineDeletionIndicator(Boolean preventOnlineDeletionIndicator) {
        this.preventOnlineDeletionIndicator = preventOnlineDeletionIndicator;
    }

    public String getTransactionDescription() { return transactionDescription; }
    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getProrationTypeCode() { return prorationTypeCode; }
    public void setProrationTypeCode(String prorationTypeCode) { this.prorationTypeCode = prorationTypeCode; }

    public String getCycleId() { return cycleId; }
    public void setCycleId(String cycleId) { this.cycleId = cycleId; }

    public Boolean getApplyToEarliestCycle() { return applyToEarliestCycle; }
    public void setApplyToEarliestCycle(Boolean applyToEarliestCycle) {
        this.applyToEarliestCycle = applyToEarliestCycle;
    }

    public String getSmeSystemSourceId() { return smeSystemSourceId; }
    public void setSmeSystemSourceId(String smeSystemSourceId) { this.smeSystemSourceId = smeSystemSourceId; }

    public String getSourceRefNum() { return sourceRefNum; }
    public void setSourceRefNum(String sourceRefNum) { this.sourceRefNum = sourceRefNum; }

    public String getPrincipalPaymentAmount() { return principalPaymentAmount; }
    public void setPrincipalPaymentAmount(String principalPaymentAmount) {
        this.principalPaymentAmount = principalPaymentAmount;
    }

    public String getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(String requestedAmount) { this.requestedAmount = requestedAmount; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getLoanAlias() { return loanAlias; }
    public void setLoanAlias(String loanAlias) { this.loanAlias = loanAlias; }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public LocalDate getCycleStartDate() { return cycleStartDate; }
    public void setCycleStartDate(LocalDate cycleStartDate) { this.cycleStartDate = cycleStartDate; }

    public LocalDateTime getCreateTimeStamp() { return createTimeStamp; }

    public LocalDateTime getUpdateTimeStamp() { return updateTimeStamp; }
}
