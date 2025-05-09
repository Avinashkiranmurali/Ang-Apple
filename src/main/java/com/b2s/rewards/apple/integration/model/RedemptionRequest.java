package com.b2s.rewards.apple.integration.model;

import java.util.List;
import java.util.Map;

/**
 * @author rperumal
 */
public class RedemptionRequest {

    private AccountIdentifier accountIdentifier;
    private SessionUserInformation sessionUserInformation;
    private Map<String,String > additionalInfo;
    private String orderId;
    private String orderDate;
    private List<RedemptionOrderLine> orderlines;
    private Integer cashBuyInPoints;
    private String cashBuyInPrice;
    private String currency;
    private ShipmentDeliveryInfo delivery;
    private Integer totalPointsPrice;
    private String totalPrice;
    private String sessionState;
    private SplitTenderInfo splitTenderInfo;
    private Integer startBalance;
    private PayrollDeductionInfo payrollDeductionInfo;
    private DiscountInfo discountInfo;


    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public SessionUserInformation getSessionUserInformation() {
        return sessionUserInformation;
    }

    public void setSessionUserInformation(SessionUserInformation sessionUserInformation) {
        this.sessionUserInformation = sessionUserInformation;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<RedemptionOrderLine> getOrderlines() {
        return orderlines;
    }

    public void setOrderlines(List<RedemptionOrderLine> orderlines) {
        this.orderlines = orderlines;
    }

    public ShipmentDeliveryInfo getDelivery() {
        return delivery;
    }

    public void setDelivery(ShipmentDeliveryInfo delivery) {
        this.delivery = delivery;
    }

    public Integer getCashBuyInPoints() {
        return cashBuyInPoints;
    }

    public void setCashBuyInPoints(Integer cashBuyInPoints) {
        this.cashBuyInPoints = cashBuyInPoints;
    }

    public String getCashBuyInPrice() {
        return cashBuyInPrice;
    }

    public void setCashBuyInPrice(String cashBuyInPrice) {
        this.cashBuyInPrice = cashBuyInPrice;
    }

    public Integer getTotalPointsPrice() { return totalPointsPrice; }

    public void setTotalPointsPrice(final Integer totalPointsPrice) { this.totalPointsPrice = totalPointsPrice; }

    public String getTotalPrice() { return totalPrice; }

    public void setTotalPrice(final String totalPrice) { this.totalPrice = totalPrice; }

    public SplitTenderInfo getSplitTenderInfo() {return splitTenderInfo; }

    public void setSplitTenderInfo(final SplitTenderInfo splitTenderInfo) { this.splitTenderInfo = splitTenderInfo;}

    public Integer getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(final Integer startBalance) {
        this.startBalance = startBalance;
    }

    public PayrollDeductionInfo getPayrollDeductionInfo() {
        return payrollDeductionInfo;
    }

    public void setPayrollDeductionInfo(final PayrollDeductionInfo payrollDeductionInfo) {
        this.payrollDeductionInfo = payrollDeductionInfo;
    }

    public DiscountInfo getDiscountInfo() {
        return discountInfo;
    }

    public void setDiscountInfo(final DiscountInfo discountInfo) {
        this.discountInfo = discountInfo;
    }

    public void setCurrency(final String currency) { this.currency = currency; }

    public String getCurrency() { return currency; }
}
