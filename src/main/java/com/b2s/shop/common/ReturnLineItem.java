package com.b2s.shop.common;

import com.b2s.rewards.apple.model.Price;

public class ReturnLineItem {
    private String productName;
    private Price itemPrice;
    private Price taxPrice;
    private Price feesPrice;
    private Integer quantity;

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public Price getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(final Price itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Price getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(final Price taxPrice) {
        this.taxPrice = taxPrice;
    }

    public Price getFeesPrice() {
        return feesPrice;
    }

    public void setFeesPrice(final Price feesPrice) {
        this.feesPrice = feesPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}
