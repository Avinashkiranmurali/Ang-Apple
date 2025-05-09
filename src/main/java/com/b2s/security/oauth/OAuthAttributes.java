package com.b2s.security.oauth;

import java.io.Serializable;

public class OAuthAttributes implements Serializable {
    private static final long serialVersionUID = 2354852686512692894L;
    private String state;
    private String code;
    private Token token;
    private String signOutUrl;
    private OAuthCredentials oAuthCredentials;

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token token) {
        this.token = token;
    }

    public String getSignOutUrl() {
        return signOutUrl;
    }

    public void setSignOutUrl(final String signOutUrl) {
        this.signOutUrl = signOutUrl;
    }

    public OAuthCredentials getoAuthCredentials() {
        return oAuthCredentials;
    }

    public void setoAuthCredentials(final OAuthCredentials oAuthCredentials) {
        this.oAuthCredentials = oAuthCredentials;
    }
}