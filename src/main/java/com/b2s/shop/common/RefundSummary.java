package com.b2s.shop.common;

import com.b2s.rewards.apple.model.Price;

import java.util.ArrayList;
import java.util.List;

public class RefundSummary {
    private List<ReturnLineItem> lineItems;
    private Price subTotal;
    private Price taxesAndFees;
    private Price total;
    private Price refunds;

    public List<ReturnLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(final List<ReturnLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Price getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(final Price subTotal) {
        this.subTotal = subTotal;
    }

    public Price getTaxesAndFees() {
        return taxesAndFees;
    }

    public void setTaxesAndFees(final Price taxesAndFees) {
        this.taxesAndFees = taxesAndFees;
    }

    public Price getRefunds() {
        return refunds;
    }

    public void setRefunds(final Price refunds) {
        this.refunds = refunds;
    }

    public Price getTotal() {
        return total;
    }

    public void setTotal(final Price total) {
        this.total = total;
    }
}