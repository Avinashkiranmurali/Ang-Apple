package com.b2s.rewards.apple.model;

import java.io.Serializable;

/**
 * Created by srukmagathan on 14-02-2017.
 */
public class PayrollDeduction implements Serializable {

    private static final long serialVersionUID = -4540199600916289753L;
    private Double pdAmount;
    private Double payPerPeriod;
    private Integer payPeriods;
    private Double cardPayment;

    public Double getPdAmount() {
        return pdAmount;
    }

    public void setPdAmount(Double pdAmount) {
        this.pdAmount = pdAmount;
    }

    public Double getPayPerPeriod() {
        return payPerPeriod;
    }

    public void setPayPerPeriod(final Double payPerPeriod) {
        this.payPerPeriod = payPerPeriod;
    }

    public Integer getPayPeriods() {
        return payPeriods;
    }

    public void setPayPeriods(final Integer payPeriods) {
        this.payPeriods = payPeriods;
    }

    public Double getCardPayment() {
        return cardPayment;
    }

    public void setCardPayment(final Double cardPayment) {
        this.cardPayment = cardPayment;
    }
}
