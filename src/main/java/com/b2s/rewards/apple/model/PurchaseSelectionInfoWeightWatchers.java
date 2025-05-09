package com.b2s.rewards.apple.model;

import java.util.Map;

/*** Created by vmurugesan on 9/22/2016.
 */
public class PurchaseSelectionInfoWeightWatchers extends PurchaseSelectionInfo{

    private Map additionalInfo;
    private String orderId;
    private String method = "GET";
    public Map getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final Map additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }
}
