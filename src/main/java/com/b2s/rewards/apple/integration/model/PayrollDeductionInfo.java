package com.b2s.rewards.apple.integration.model;

/**
 * Created by srukmagathan on 04-05-2017.
 */
public class PayrollDeductionInfo {

    private String totalPrice;

    private String perPayPeriodPrice;

    private String numberOfPayPeriods;

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(final String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPerPayPeriodPrice() {
        return perPayPeriodPrice;
    }

    public void setPerPayPeriodPrice(final String perPayPeriodPrice) {
        this.perPayPeriodPrice = perPayPeriodPrice;
    }

    public String getNumberOfPayPeriods() {
        return numberOfPayPeriods;
    }

    public void setNumberOfPayPeriods(final String numberOfPayPeriods) {
        this.numberOfPayPeriods = numberOfPayPeriods;
    }
}
