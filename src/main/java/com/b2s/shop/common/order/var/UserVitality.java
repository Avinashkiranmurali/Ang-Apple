package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

public class UserVitality extends User {

    private static final long serialVersionUID = -2027135437079488822L;

    private String activationFee;
    private String financedAmount;
    private String maxMonthlyPayment;
    private int programLength;
    private String delta;

    public String getActivationFee() {
        return activationFee;
    }

    public void setActivationFee(final String activationFee) {
        this.activationFee = activationFee;
    }

    public String getFinancedAmount() {
        return financedAmount;
    }

    public void setFinancedAmount(final String financedAmount) {
        this.financedAmount = financedAmount;
    }

    public String getMaxMonthlyPayment() {
        return maxMonthlyPayment;
    }

    public void setMaxMonthlyPayment(final String maxMonthlyPayment) {
        this.maxMonthlyPayment = maxMonthlyPayment;
    }

    public int getProgramLength() {
        return programLength;
    }

    public void setProgramLength(final int programLength) {
        this.programLength = programLength;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(final String delta) {
        this.delta = delta;
    }
}