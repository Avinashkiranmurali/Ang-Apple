package com.b2s.rewards.apple.model;

import java.io.Serializable;

public class Tax implements Serializable {

    private static final long serialVersionUID = 2404766687354838839L;
    private String taxId;
    private Price amount;

    public Tax(final String taxId, final Price amount) {
        this.taxId = taxId;
        this.amount = amount;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(final String taxId) {
        this.taxId = taxId;
    }

    public Price getAmount() {
        return amount;
    }

    public void setAmount(final Price amount) {
        this.amount = amount;
    }
}
