package com.b2s.rewards.apple.integration.model;

public class SessionRequest {

    private String authorizationToken;
    private String accountSessionId;
    private String userid;

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(final String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }
    public String getAccountSessionId() {
        return accountSessionId;
    }

    public void setAccountSessionId(final String accountSessionId) {
        this.accountSessionId = accountSessionId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(final String userid) {
        this.userid = userid;
    }

}
