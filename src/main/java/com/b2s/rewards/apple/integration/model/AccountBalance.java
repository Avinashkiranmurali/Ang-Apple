package com.b2s.rewards.apple.integration.model;

/**
 * @author rperumal
 */
public class AccountBalance {

    private int pointsBalance;
    private String pointsName;
    private String currency;


    public int getPointsBalance() { return pointsBalance; }

    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public String getPointsName() {
        return pointsName;
    }

    public void setPointsName(String pointsName) {
        this.pointsName = pointsName;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

}
