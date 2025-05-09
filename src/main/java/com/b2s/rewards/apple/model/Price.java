package com.b2s.rewards.apple.model;

import org.joda.money.Money;

import java.io.Serializable;

/**
 * Represents a price in money and points.
 *
 * @author rperumal
 */
public class Price implements Serializable {

    private static final long serialVersionUID = -4536179608596736002L;
    private Double amount = 0.0;
    private String currencyCode = "";
    private int points;

    public Price() {
    }

    public Price(Double amount, String currencyCode, int points) {
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.points = points;
    }
    public Price(final Money money, final int points) {
        this.amount = money.getAmount().doubleValue();
        this.currencyCode = money.getCurrencyUnit().getCode();
        this.points = points;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public int getPoints() {
        return points;
    }
}
