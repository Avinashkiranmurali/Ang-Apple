package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.util.AppleUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*** Created by ssrinivasan on 7/1/2015.
 */
public class OrderStatus implements Serializable {

    private static final long serialVersionUID = -3559638022211597537L;
    private Long b2sOrderId;
    private String varOrderId;
    private String b2rMessage;
    private String varMessage;
    private String ccType;
    private String last4;
    private boolean emptyCart = true;
    private List<DiscountCode> discountCodes;
    private boolean discountCodeError=false;
    private Integer orderHoldDurationInDays;
    private Date orderDate;
    private PayrollDeduction payrollDeduction;
    private boolean promotionUseExceeded = false;
    private boolean timedOut = false;
    private boolean showVarOrderId = false;
    private String errorCode;
    private Integer payrollAgreementId;
    private String payrollAgreementUrl;
    private int earnedPoints;


    private CartTotal cartTotal;

    public boolean isPromotionUseExceeded() {
        return promotionUseExceeded;
    }

    public void setPromotionUseExceeded(boolean promotionUseExceeded) {
        this.promotionUseExceeded = promotionUseExceeded;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public Integer getOrderHoldDurationInDays() {
        return orderHoldDurationInDays;
    }

    public void setOrderHoldDurationInDays(Integer orderHoldDurationInDays) {
        this.orderHoldDurationInDays = orderHoldDurationInDays;
    }

    public boolean isEmptyCart() {
        return emptyCart;
    }

    public void setEmptyCart(boolean emptyCart) {
        this.emptyCart = emptyCart;
    }

    public boolean isDiscountCodeError() {
        return discountCodeError;
    }

    public void setDiscountCodeError(final boolean discountCodeError) {
        this.discountCodeError = discountCodeError;
    }

    public List<DiscountCode> getDiscountCodes() {
        return discountCodes;
    }

    public void setDiscountCodes(final List<DiscountCode> discountCodes) {
        this.discountCodes = discountCodes;
    }

    public String getCcType() {
        return ccType;
    }

    public void setCcType(final String ccType) {
        this.ccType = ccType;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(final String last4) {
        this.last4 = last4;
    }

    private boolean orderCompleted = false;

    public Long getB2sOrderId() {
        return (b2sOrderId == null ? 0 : b2sOrderId);
    }

    public void setB2sOrderId(Long b2sOrderId) {
        this.b2sOrderId = b2sOrderId;
    }

    public String getVarOrderId() {
        return AppleUtil.replaceNull(varOrderId);
    }

    public void setVarOrderId(String varOrderId) {
        this.varOrderId = varOrderId;
    }

    public String getB2rMessage() {
        return AppleUtil.replaceNull(b2rMessage);
    }

    public void setB2rMessage(String b2rMessage) {
        this.b2rMessage = b2rMessage;
    }

    public String getVarMessage() {
        return AppleUtil.replaceNull(varMessage);
    }

    public void setVarMessage(String varMessage) {
        this.varMessage = varMessage;
    }

    public boolean isOrderCompleted() {
        return orderCompleted;
    }

    public void setOrderCompleted(boolean orderCompleted) {
        this.orderCompleted = orderCompleted;
    }

    public CartTotal getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(CartTotal cartTotal) {
        this.cartTotal = cartTotal;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }

    public PayrollDeduction getPayrollDeduction() {
        return payrollDeduction;
    }

    public void setPayrollDeduction(final PayrollDeduction payrollDeduction) {
        this.payrollDeduction = payrollDeduction;
    }

    public boolean isShowVarOrderId() {
        return showVarOrderId;
    }

    public void setShowVarOrderId(boolean showVarOrderId) {
        this.showVarOrderId = showVarOrderId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getPayrollAgreementId() {
        return payrollAgreementId;
    }

    public void setPayrollAgreementId(Integer payrollAgreementId) {
        this.payrollAgreementId = payrollAgreementId;
    }

    public String getPayrollAgreementUrl() {
        return payrollAgreementUrl;
    }

    public void setPayrollAgreementUrl(String payrollAgreementUrl) {
        this.payrollAgreementUrl = payrollAgreementUrl;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(final int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }
}
