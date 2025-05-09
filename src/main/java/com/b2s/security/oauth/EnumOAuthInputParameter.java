package com.b2s.security.oauth;

public enum EnumOAuthInputParameter {
    GRANT_TYPE("grant_type"),
    CLIENT_ID("client_id"),
    CLIENT_SECRET("client_secret"),
    CODE("code"),
    SCOPE("scope"),
    REDIRECT_URI("redirect_uri"),
    RESPONSE_TYPE("response_type"),
    REFRESH_TOKEN("refresh_token"),
    AUTH_STATE("state"),
    AUTHORIZATION("Authorization");

    private final String value;

    EnumOAuthInputParameter(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
