package com.b2s.rewards.apple.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by rpillai on 7/5/2016.
 */
public class CreditItem implements Serializable {

    private static final long serialVersionUID = 1166972633133800995L;
    private double baseItemPrice = 0;        // Pre-cc-marked-up price
    private double b2sProfit = 0;
    private double varProfit = 0;
    private Money varPrice;
    private CurrencyUnit currency;
    private double itemTotal = -1;

    private String creditCardType;
    private String ccFirstName;
    private String ccLastName;
    private String ccLast4;
    private double b2sMargin;
    private double varMargin;

    //Creating ccVarMargin instead of reusing varMargin(used for CITI) in order to directly convert to Float
    private BigDecimal ccVarMargin;
    private BigDecimal effectiveConversionRate;
    private Integer pointsPurchased;


    public double getBaseItemPrice() {
        return baseItemPrice;
    }

    public void setBaseItemPrice(double baseItemPrice) {
        this.baseItemPrice = baseItemPrice;
    }

    public double getB2sProfit() {
        return b2sProfit;
    }

    public void setB2sProfit(double b2sProfit) {
        this.b2sProfit = b2sProfit;
    }

    public double getVarProfit() {
        return varProfit;
    }

    public void setVarProfit(double varProfit) {
        this.varProfit = varProfit;
    }

    public Money getVarPrice() {
        return varPrice;
    }

    public void setVarPrice(final Money varPrice) {
        this.varPrice = varPrice;
    }

    public CurrencyUnit getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyUnit currency) {
        this.currency = currency;
    }

    public double getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(double itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getCcFirstName() {
        return ccFirstName;
    }

    public void setCcFirstName(String ccFirstName) {
        this.ccFirstName = ccFirstName;
    }

    public String getCcLastName() {
        return ccLastName;
    }

    public void setCcLastName(String ccLastName) {
        this.ccLastName = ccLastName;
    }

    public String getCcLast4() {
        return ccLast4;
    }

    public void setCcLast4(final String ccLast4) {
        this.ccLast4 = ccLast4;
    }

    public double getB2sMargin() {
        return b2sMargin;
    }

    public void setB2sMargin(double b2sMargin) {
        this.b2sMargin = b2sMargin;
    }

    public double getVarMargin() {
        return varMargin;
    }

    public void setVarMargin(double varMargin) {
        this.varMargin = varMargin;
    }

    public BigDecimal getCcVarMargin() {
        return ccVarMargin;
    }

    public void setCcVarMargin(final BigDecimal ccVarMargin) {
        this.ccVarMargin = ccVarMargin;
    }

    public BigDecimal getEffectiveConversionRate() {
        return effectiveConversionRate;
    }

    public void setEffectiveConversionRate(final BigDecimal effectiveConversionRate) {
        this.effectiveConversionRate = effectiveConversionRate;
    }

    public Integer getPointsPurchased() {
        return pointsPurchased;
    }

    public void setPointsPurchased(final Integer pointsPurchased) {
        this.pointsPurchased = pointsPurchased;
    }
}
