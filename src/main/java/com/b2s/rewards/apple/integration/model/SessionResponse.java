package com.b2s.rewards.apple.integration.model;

public class SessionResponse {

    private String sessionId;
    private String accountId;
    private AccountInfo accountInfo;


    public String getSessionId() {
        return sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public void setAccountInfo(final AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }
}
