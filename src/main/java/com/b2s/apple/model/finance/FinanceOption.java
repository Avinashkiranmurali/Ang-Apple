package com.b2s.apple.model.finance;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Currency;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinanceOption {

    private String varId;
    private String programId;
    private Integer installment;
    private String installmentPeriod;
    private String messageCode;
    private Boolean isActive;
    private Integer orderBy;
    private Currency currency;//The currency value
    private Double installmentAmount;
    private String establishmentFeeType;
    private Float establishmentFeeRate;
    private Float establishmentFeeAmt;

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public Integer getInstallment() {
        return installment;
    }

    public void setInstallment(Integer installment) {
        this.installment = installment;
    }

    public String getInstallmentPeriod() {
        return installmentPeriod;
    }

    public void setInstallmentPeriod(String installmentPeriod) {
        this.installmentPeriod = installmentPeriod;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Integer orderBy) {
        this.orderBy = orderBy;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Double getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(Double installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public String getEstablishmentFeeType() {
        return establishmentFeeType;
    }

    public void setEstablishmentFeeType(String establishmentFeeType) {
        this.establishmentFeeType = establishmentFeeType;
    }

    public Float getEstablishmentFeeRate() {
        return establishmentFeeRate;
    }

    public void setEstablishmentFeeRate(Float establishmentFeeRate) {
        this.establishmentFeeRate = establishmentFeeRate;
    }

    public Float getEstablishmentFeeAmt() {
        return establishmentFeeAmt;
    }

    public void setEstablishmentFeeAmt(Float establishmentFeeAmt) {
        this.establishmentFeeAmt = establishmentFeeAmt;
    }
}
