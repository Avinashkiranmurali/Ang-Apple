package com.b2s.rewards.apple.integration.model;

import java.util.Map;

/**
 * Created by rpillai on 10/10/2016.
 */
public class CancelRedemptionRequest {
    private String varId;
    private String orderId;
    private String varOrderId;
    private Map<String, String> additionalInfo;
    private Integer totalPointRefund;
    private AccountIdentifier accountIdentifier;

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVarOrderId() {
        return varOrderId;
    }

    public void setVarOrderId(String varOrderId) {
        this.varOrderId = varOrderId;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Integer getTotalPointRefund() {
        return totalPointRefund;
    }

    public void setTotalPointRefund(Integer totalPointRefund) {
        this.totalPointRefund = totalPointRefund;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(final AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }
}
