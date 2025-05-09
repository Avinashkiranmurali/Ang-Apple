package com.b2s.rewards.apple.model;

import java.io.Serializable;

/**
 * Created by rperumal on 2/11/2016
 *
 * Defines different pricing model structure for each market (US, UK, CA etc...)
 *
 */

public class PricingModel implements Serializable {

    private static final long serialVersionUID = 2772182300174238117L;
    private String market;
    private String priceType;
    private String priceKey;
    private Double paymentValue;
    private Integer paymentValuePoints;
    private Integer repaymentTerm;
    private Integer monthsSubsidized;
    private Double upgradeCost;
    private Double activationFee;
    private Double totalDueTodayBeforeTax;
    private Double delta;
    private Double discountTier1;
    private Double discountTier2;
    private Double discountTier3;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getPriceKey() {
        return priceKey;
    }

    public void setPriceKey(String priceKey) {
        this.priceKey = priceKey;
    }

    public Double getPaymentValue() {
        return paymentValue;
    }

    public void setPaymentValue(Double paymentValue) {
        this.paymentValue = paymentValue;
    }

    public Integer getPaymentValuePoints() {
        return paymentValuePoints;
    }

    public void setPaymentValuePoints(Integer paymentValuePoints) {
        this.paymentValuePoints = paymentValuePoints;
    }

    public Integer getRepaymentTerm() {
        return repaymentTerm;
    }

    public void setRepaymentTerm(Integer repaymentTerm) {
        this.repaymentTerm = repaymentTerm;
    }

    public Integer getMonthsSubsidized() {
        return monthsSubsidized;
    }

    public void setMonthsSubsidized(Integer monthsSubsidized) {
        this.monthsSubsidized = monthsSubsidized;
    }

    public void setUpgradeCost(Double upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public Double getUpgradeCost() {
        return upgradeCost;
    }

    public Double getActivationFee() {
        return activationFee;
    }

    public void setActivationFee(Double activationFee) {
        this.activationFee = activationFee;
    }

    public Double getTotalDueTodayBeforeTax() {
        return totalDueTodayBeforeTax;
    }

    public void setTotalDueTodayBeforeTax(Double totalDueTodayBeforeTax) {
        this.totalDueTodayBeforeTax = totalDueTodayBeforeTax;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public Double getDiscountTier1() {
        return discountTier1;
    }

    public void setDiscountTier1(Double discountTier1) {
        this.discountTier1 = discountTier1;
    }

    public Double getDiscountTier2() {
        return discountTier2;
    }

    public void setDiscountTier2(Double discountTier2) {
        this.discountTier2 = discountTier2;
    }

    public Double getDiscountTier3() {
        return discountTier3;
    }

    public void setDiscountTier3(Double discountTier3) {
        this.discountTier3 = discountTier3;
    }
}