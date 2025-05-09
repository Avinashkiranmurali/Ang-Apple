package com.b2s.security.oauth;

public enum EnumOAuthKey {
    AUTH_URL(".authorizationUrl"),
    TOKEN_URL(".tokenUrl"),
    CLIENT_ID(".clientId"),
    CLIENT_SECRET(".clientSecret"),
    GRANT_TYPE_AUTH(".grantTypeAuth"),
    GRANT_TYPE_REFRESH(".grantTypeRefresh"),
    SCOPE(".scope"),
    REDIRECT_URI(".redirectUri"),
    END_SESSION_URI(".endSession"),
    CHECK_SESSION_IFRAME_URI(".checkSessionIframe"),
    USER_INFO(".userInfo"),
    OKTA_FLAG(".oktaFlag"),
    SERVICE_ACCOUNT_ID(".serviceAccountId"),
    SERVICE_ACCOUNT_PASSWORD(".serviceAccountPassword");

    private final String value;

    EnumOAuthKey(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
