package com.b2s.rewards.apple.model;

public class CreateTransactionResponse {
    private String url;
    private String transactionId;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }
}
