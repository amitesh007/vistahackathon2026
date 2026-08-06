package com.loanservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;

/**
 * Request/response model for all LoanInterestPayment operations
 * (Create, Update, GetById, Delete).
 * Fields not applicable to a given operation will simply be null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanInterestPaymentModel {

    @JsonProperty("className")
    private String className;

    @JsonProperty("transaction")
    private String transaction;

    // ------ Create fields ------

    @JsonProperty("transactionDate")
    private LocalDate transactionDate;

    @JsonProperty("eventComment")
    private String eventComment;

    @JsonProperty("preventOnlineDeletionIndicator")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean preventOnlineDeletionIndicator;

    @JsonProperty("transactionDescription")
    private String transactionDescription;

    @JsonProperty("prorationTypeCode")
    private String prorationTypeCode;

    @JsonProperty("cycleId")
    private String cycleId;

    @JsonProperty("applyToEarliestCycle")
    @JsonDeserialize(using = YNBooleanDeserializer.class)
    private Boolean applyToEarliestCycle;

    @JsonProperty("smeSystemSourceId")
    private String smeSystemSourceId;

    @JsonProperty("sourceRefNum")
    private String sourceRefNum;

    @JsonProperty("principalPaymentAmount")
    private String principalPaymentAmount;

    // ------ Update / GetById / Delete fields ------

    @JsonProperty("loanTransactionId")
    private String loanTransactionId;

    @JsonProperty("requestedAmount")
    private String requestedAmount;

    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    @JsonProperty("loanAlias")
    private String loanAlias;

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("cycleStartDate")
    private LocalDate cycleStartDate;

    // ---- Getters & Setters ----

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTransaction() { return transaction; }
    public void setTransaction(String transaction) { this.transaction = transaction; }

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

    public String getLoanTransactionId() { return loanTransactionId; }
    public void setLoanTransactionId(String loanTransactionId) { this.loanTransactionId = loanTransactionId; }

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
}
