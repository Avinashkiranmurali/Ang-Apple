package com.b2s.shop.common.order.util;

/**
 * Created by ssrinivasan on 12/7/2016.
 */
public enum OAuthRequestParam {
    VARID("varId"),
    PROGRAM_ID("programId"),
    MEMBER_ID("memberID"),
    ONLINE_AUTH_CODE("onlineAuthCode"),
    OFFLINE_AUTH_CODE("offlineAuthCode"),
    CODE("code"),
    OKTA("okta");

    private final String value;

    OAuthRequestParam(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}