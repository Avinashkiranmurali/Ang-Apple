package com.b2s.rewards.apple.model;

import java.math.BigDecimal;

public class SplitTenderCart {
    private BigDecimal cashAmount;
    private Integer earnPoints;

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(final BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
    }

    public Integer getEarnPoints() {
        return earnPoints;
    }

    public void setEarnPoints(final Integer earnPoints) {
        this.earnPoints = earnPoints;
    }
}
