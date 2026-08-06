package com.loanservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;

/**
 * Unified request model covering all four operations (Create, Update, GetById, Delete).
 * Fields not applicable to a given operation will simply be null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanRequest {

    @JsonProperty("className")
    private String className;

    @JsonProperty("transaction")
    private String transaction;

    // ------ Create / Update fields ------

    @JsonProperty("requestedAmount")
    private String requestedAmount;

    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    @JsonProperty("eventComment")
    private String eventComment;

    @JsonProperty("preventOnlineDeletionIndicator")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean preventOnlineDeletionIndicator;

    @JsonProperty("transactionDescription")
    private String transactionDescription;

    @JsonProperty("loanAlias")
    private String loanAlias;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("suppressBreakfunding")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean suppressBreakfunding;

    @JsonProperty("transactionDate")
    private LocalDate transactionDate;

    @JsonProperty("systemSourceId")
    private String systemSourceId;

    @JsonProperty("sourceRefNum")
    private String sourceRefNum;

    @JsonProperty("autoReduceFacility")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean autoReduceFacility;

    // ------ Update / GetById / Delete fields ------

    @JsonProperty("loanTransactionId")
    private String loanTransactionId;

    // ------ Update-only fields ------

    @JsonProperty("applyToEarliestItem")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean applyToEarliestItem;

    @JsonProperty("scheduleDate")
    private LocalDate scheduleDate;

    // ------ LoanInterestPayment fields ------

    @JsonProperty("prorationTypeCode")
    private String prorationTypeCode;

    @JsonProperty("cycleId")
    private String cycleId;

    @JsonProperty("applyToEarliestCycle")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean applyToEarliestCycle;

    @JsonProperty("smeSystemSourceId")
    private String smeSystemSourceId;

    @JsonProperty("principalPaymentAmount")
    private String principalPaymentAmount;

    @JsonProperty("cycleStartDate")
    private LocalDate cycleStartDate;

    // ---- Getters & Setters ----

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTransaction() { return transaction; }
    public void setTransaction(String transaction) { this.transaction = transaction; }

    public String getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(String requestedAmount) { this.requestedAmount = requestedAmount; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getEventComment() { return eventComment; }
    public void setEventComment(String eventComment) { this.eventComment = eventComment; }

    public Boolean getPreventOnlineDeletionIndicator() { return preventOnlineDeletionIndicator; }
    public void setPreventOnlineDeletionIndicator(Boolean v) { this.preventOnlineDeletionIndicator = v; }

    public String getTransactionDescription() { return transactionDescription; }
    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getLoanAlias() { return loanAlias; }
    public void setLoanAlias(String loanAlias) { this.loanAlias = loanAlias; }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public Boolean getSuppressBreakfunding() { return suppressBreakfunding; }
    public void setSuppressBreakfunding(Boolean suppressBreakfunding) {
        this.suppressBreakfunding = suppressBreakfunding;
    }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getSystemSourceId() { return systemSourceId; }
    public void setSystemSourceId(String systemSourceId) { this.systemSourceId = systemSourceId; }

    public String getSourceRefNum() { return sourceRefNum; }
    public void setSourceRefNum(String sourceRefNum) { this.sourceRefNum = sourceRefNum; }

    public Boolean getAutoReduceFacility() { return autoReduceFacility; }
    public void setAutoReduceFacility(Boolean autoReduceFacility) { this.autoReduceFacility = autoReduceFacility; }

    public String getLoanTransactionId() { return loanTransactionId; }
    public void setLoanTransactionId(String loanTransactionId) { this.loanTransactionId = loanTransactionId; }

    public Boolean getApplyToEarliestItem() { return applyToEarliestItem; }
    public void setApplyToEarliestItem(Boolean applyToEarliestItem) { this.applyToEarliestItem = applyToEarliestItem; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

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

    public String getPrincipalPaymentAmount() { return principalPaymentAmount; }
    public void setPrincipalPaymentAmount(String principalPaymentAmount) {
        this.principalPaymentAmount = principalPaymentAmount;
    }

    public LocalDate getCycleStartDate() { return cycleStartDate; }
    public void setCycleStartDate(LocalDate cycleStartDate) { this.cycleStartDate = cycleStartDate; }
}
