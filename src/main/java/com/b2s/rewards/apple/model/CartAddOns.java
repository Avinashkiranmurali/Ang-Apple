package com.b2s.rewards.apple.model;

import java.io.Serializable;

public class CartAddOns implements Serializable {
    private static final long serialVersionUID = 6107299223719068479L;

    private CartItem servicePlan;
    private CartItem giftItem;

    public CartItem getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(CartItem servicePlan) {
        this.servicePlan = servicePlan;
    }

    public CartItem getGiftItem() {
        return giftItem;
    }

    public void setGiftItem(CartItem giftItem) {
        this.giftItem = giftItem;
    }
}