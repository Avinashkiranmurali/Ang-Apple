package com.b2s.rewards.apple.model;


import java.io.Serializable;

/**
 * Fees applied in the price calculation.
 *
 * @author rperumal
 */
public class Fee implements Serializable {

    private static final long serialVersionUID = -4030843411447195266L;
    private String feeId = "";
    private Price amount = new Price();
    public Fee(){

    }
    public Fee(final String feeId, final Price amount) {
        this.feeId = feeId;
        this.amount = amount;
    }

    public Price getAmount() {
        return amount;
    }

    public void setAmount(final Price amount) {
        this.amount = amount;
    }

    public String getFeeId() {
        return feeId;
    }

    public void setFeeId(String feeId) {
        this.feeId = feeId;
    }
}
