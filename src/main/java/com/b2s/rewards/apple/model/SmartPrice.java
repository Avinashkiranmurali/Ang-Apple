package com.b2s.rewards.apple.model;

import java.io.Serializable;

public class SmartPrice implements Serializable {

    private static final long serialVersionUID = 1302173331840781223L;

    private Double amount = 0.0;
    private String currencyCode = "";
    private int points;
    private boolean isCashMaxLimitReached;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean getIsCashMaxLimitReached() {
        return isCashMaxLimitReached;
    }

    public void setIsCashMaxLimitReached(boolean cashMaxLimitReached) {
        isCashMaxLimitReached = cashMaxLimitReached;
    }
}
