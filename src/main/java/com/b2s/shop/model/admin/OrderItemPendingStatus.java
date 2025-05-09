package com.b2s.shop.model.admin;

/**
 * User: bsalvati@bridge2solutions.com
 */
public class OrderItemPendingStatus {

    final private String stockStatus;
    final private int quantityLimit;

    public OrderItemPendingStatus(String stockStatus, int quantityLimit) {
        this.stockStatus = stockStatus;
        this.quantityLimit = quantityLimit;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public int getQuantityLimit() {
        return quantityLimit;
    }
}
