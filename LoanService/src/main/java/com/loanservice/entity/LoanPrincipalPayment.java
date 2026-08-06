package com.loanservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loanservice.model.YNBooleanSerializer;

/**
 * JPA entity that backs the LOAN_PRINCIPAL_PAYMENT table in H2.
 * Covers every field that may be submitted across create / update operations.
 */
@Entity
@Table(name = "LOAN_PRINCIPAL_PAYMENT")
public class LoanPrincipalPayment {

    @Id
    @Column(name = "LOAN_TRANSACTION_ID", length = 24)
    private String loanTransactionId;

    @Column(name = "TRANSACTION_TYPE", length = 50)
    private String transactionType;

    @Column(name = "REQUESTED_AMOUNT", length = 20)
    private String requestedAmount;

    @Column(name = "EFFECTIVE_DATE")
    private LocalDate effectiveDate;

    @Column(name = "EVENT_COMMENT", length = 255)
    private String eventComment;

    @Column(name = "PREVENT_ONLINE_DELETION_INDICATOR")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean preventOnlineDeletionIndicator = Boolean.FALSE;

    @Column(name = "TRANSACTION_DESCRIPTION", length = 255)
    private String transactionDescription;

    @Column(name = "LOAN_ALIAS", length = 104)
    private String loanAlias;

    @Column(name = "LOAN_ID", length = 106)
    private String loanId;

    @Column(name = "SUPPRESS_BREAKFUNDING")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean suppressBreakfunding = Boolean.FALSE;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "SYSTEM_SOURCE_ID", length = 50)
    private String systemSourceId;

    @Column(name = "SOURCE_REF_NUM", length = 50)
    private String sourceRefNum;

    @Column(name = "AUTO_REDUCE_FACILITY")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean autoReduceFacility = Boolean.FALSE;

    @Column(name = "APPLY_TO_EARLIEST_ITEM")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean applyToEarliestItem = Boolean.FALSE;

    @Column(name = "SCHEDULE_DATE")
    private LocalDate scheduleDate;

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

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(String requestedAmount) { this.requestedAmount = requestedAmount; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getEventComment() { return eventComment; }
    public void setEventComment(String eventComment) { this.eventComment = eventComment; }

    public Boolean getPreventOnlineDeletionIndicator() { return preventOnlineDeletionIndicator; }
    public void setPreventOnlineDeletionIndicator(Boolean v) { this.preventOnlineDeletionIndicator = v; }

    public String getTransactionDescription() { return transactionDescription; }
    public void setTransactionDescription(String v) { this.transactionDescription = v; }

    public String getLoanAlias() { return loanAlias; }
    public void setLoanAlias(String loanAlias) { this.loanAlias = loanAlias; }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public Boolean getSuppressBreakfunding() { return suppressBreakfunding; }
    public void setSuppressBreakfunding(Boolean suppressBreakfunding) { this.suppressBreakfunding = suppressBreakfunding; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getSystemSourceId() { return systemSourceId; }
    public void setSystemSourceId(String systemSourceId) { this.systemSourceId = systemSourceId; }

    public String getSourceRefNum() { return sourceRefNum; }
    public void setSourceRefNum(String sourceRefNum) { this.sourceRefNum = sourceRefNum; }

    public Boolean getAutoReduceFacility() { return autoReduceFacility; }
    public void setAutoReduceFacility(Boolean autoReduceFacility) { this.autoReduceFacility = autoReduceFacility; }

    public Boolean getApplyToEarliestItem() { return applyToEarliestItem; }
    public void setApplyToEarliestItem(Boolean applyToEarliestItem) { this.applyToEarliestItem = applyToEarliestItem; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public LocalDateTime getCreateTimeStamp() { return createTimeStamp; }

    public LocalDateTime getUpdateTimeStamp() { return updateTimeStamp; }
}
