package com.b2s.rewards.apple.integration.model;

import java.util.List;
import java.util.Map;

/**
 * @author rperumal
 */
public class PartialCancelRedemptionRequest {

    private String orderId;
    private String varOrderId;
    private String cancellationId;
    private String cashBuyInRefund;
    private AccountIdentifier accountIdentifier;
    private String currency;
    private List<OrderLineCancellation> orderLines;
    private String cancelDateTime;
    private Map<String, String> additionalInfo;
    private boolean fullReturn;

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

    public String getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(String cancellationId) {
        this.cancellationId = cancellationId;
    }

    public String getCashBuyInRefund() {
        return cashBuyInRefund;
    }

    public void setCashBuyInRefund(String cashBuyInRefund) {
        this.cashBuyInRefund = cashBuyInRefund;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public List<OrderLineCancellation> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLineCancellation> orderLines) {
        this.orderLines = orderLines;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

    public String getCancelDateTime() {
        return cancelDateTime;
    }

    public void setCancelDateTime(String cancelDateTime) {
        this.cancelDateTime = cancelDateTime;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setFullReturn(boolean fullReturn) {
        this.fullReturn = fullReturn;
    }

    public boolean isFullReturn() {
        return fullReturn;
    }
}
