package com.b2s.rewards.apple.model;

import org.apache.commons.lang.BooleanUtils;

/**
 * Created by msankaradoss on 1/3/2019.
 */
public class PlaceOrderRequest {

    private Boolean isPromotionChecked = false;
    private String transactionId;

    public Boolean getIsPromotionChecked() {
        return BooleanUtils.isTrue(isPromotionChecked);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }
}
