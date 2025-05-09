package com.b2s.rewards.apple.model;

public class BalanceTokenResponse {
    private String token;
    private String keyId;

    public BalanceTokenResponse(final String token, final String keyId) {
        this.token = token;
        this.keyId = keyId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }
}
