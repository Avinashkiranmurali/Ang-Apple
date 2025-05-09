package com.b2s.security.oauth;

import java.io.Serializable;

public class OAuthCredentials implements Serializable {
    private static final long serialVersionUID = -4239097724905500353L;
    private String authUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String redirectUri;
    private String endSession;
    private String grantTypeAuth;
    private String grantTypeRefresh;
    private String checkSessionIframe;
    private String serviceAccountId;
    private String serviceAccountPassword;

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(final String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(final String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getEndSession() {
        return endSession;
    }

    public void setEndSession(final String endSession) {
        this.endSession = endSession;
    }

    public String getGrantTypeAuth() {
        return grantTypeAuth;
    }

    public void setGrantTypeAuth(final String grantTypeAuth) {
        this.grantTypeAuth = grantTypeAuth;
    }

    public String getGrantTypeRefresh() {
        return grantTypeRefresh;
    }

    public void setGrantTypeRefresh(final String grantTypeRefresh) {
        this.grantTypeRefresh = grantTypeRefresh;
    }

    public String getCheckSessionIframe() {
        return checkSessionIframe;
    }

    public void setCheckSessionIframe(final String checkSessionIframe) {
        this.checkSessionIframe = checkSessionIframe;
    }

    public String getServiceAccountId() {
        return serviceAccountId;
    }

    public void setServiceAccountId(String serviceAccountId) {
        this.serviceAccountId = serviceAccountId;
    }

    public String getServiceAccountPassword() {
        return serviceAccountPassword;
    }

    public void setServiceAccountPassword(String serviceAccountPassword) {
        this.serviceAccountPassword = serviceAccountPassword;
    }
}
