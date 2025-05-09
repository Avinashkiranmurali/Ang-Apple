package com.b2s.rewards.apple.integration.model;

/**
 * Created by srukmagathan on 30-12-2016.
 */
public class SplitTenderInfo {

    private Address billingAddress;

    private String firstName;

    private String lastName;

    private String creditCardType;

    private String lastFourDigitsOfCreditCard;

    private String cash;     //The amount of money used for the split tender transaction. This should not be negative
    // . deprecated version is CashBuyInPrice

    private Integer pointsPurchased; //Points purchased for the split tender transaction. This should be greater than
    // 0.  deprecated version is CashBuyInPoints

    private Float ccVarMargin;

    private Float processingFeeRate;

    private String ccVarPrice;
    private String ccVarProfit;
    private String effectiveConversionRate;

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(final String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getLastFourDigitsOfCreditCard() {
        return lastFourDigitsOfCreditCard;
    }

    public void setLastFourDigitsOfCreditCard(final String lastFourDigitsOfCreditCard) {
        this.lastFourDigitsOfCreditCard = lastFourDigitsOfCreditCard;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(final String cash) {
        this.cash = cash;
    }

    public Integer getPointsPurchased() {
        return pointsPurchased;
    }

    public void setPointsPurchased(final Integer pointsPurchased) {
        this.pointsPurchased = pointsPurchased;
    }

    public Float getCcVarMargin(){return ccVarMargin;}

    public void setCcVarMargin(final Float ccMargin){ ccVarMargin = ccMargin; }

    public Float getProcessingFeeRate(){ return processingFeeRate; }

    public void setProcessingFeeRate(final Float processingFeeRate){ this.processingFeeRate = processingFeeRate; }

    public String getCcVarPrice() {
        return ccVarPrice;
    }

    public void setCcVarPrice(final String ccVarPrice) {
        this.ccVarPrice = ccVarPrice;
    }

    public String getCcVarProfit() {
        return ccVarProfit;
    }

    public void setCcVarProfit(final String ccVarProfit) {
        this.ccVarProfit = ccVarProfit;
    }

    public String getEffectiveConversionRate() {
        return effectiveConversionRate;
    }

    public void setEffectiveConversionRate(final String effectiveConversionRate) {
        this.effectiveConversionRate = effectiveConversionRate;
    }
}
