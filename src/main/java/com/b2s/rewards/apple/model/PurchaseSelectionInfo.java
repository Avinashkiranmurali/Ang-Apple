package com.b2s.rewards.apple.model;

public abstract class PurchaseSelectionInfo {

    private String method = "POST";
    private String purchasePostUrl;

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getPurchasePostUrl() {
        return this.purchasePostUrl;
    }

    public void setPurchasePostUrl( final String purchasePostUrl) {
        this.purchasePostUrl = purchasePostUrl;
    }

}
