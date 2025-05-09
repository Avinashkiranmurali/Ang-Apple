package com.b2s.rewards.apple.model;

public class PaymentInfo {
    private int awardsUsed;
    private int awardsPurchased; //Points equivalent for CC purchased
    private double awardsPurchasedPrice;
    private String ccLast4;

    public int getAwardsUsed() {
        return awardsUsed;
    }

    public void setAwardsUsed(final int awardsUsed) {
        this.awardsUsed = awardsUsed;
    }

    public int getAwardsPurchased() {
        return awardsPurchased;
    }

    public void setAwardsPurchased(final int awardsPurchased) {
        this.awardsPurchased = awardsPurchased;
    }

    public double getAwardsPurchasedPrice() {
        return awardsPurchasedPrice;
    }

    public void setAwardsPurchasedPrice(final double awardsPurchasedPrice) {
        this.awardsPurchasedPrice = awardsPurchasedPrice;
    }

    public String getCcLast4() {
        return ccLast4;
    }

    public void setCcLast4(final String ccLast4) {
        this.ccLast4 = ccLast4;
    }

    @Override
    public String toString() {
        return "PaymentInfo{" +
            "awardsUsed=" + awardsUsed +
            ", awardsPurchased=" + awardsPurchased +
            ", awardsPurchasedPrice=" + awardsPurchasedPrice +
            ", ccLast4='" + ccLast4 + '\'' +
            '}';
    }
}
