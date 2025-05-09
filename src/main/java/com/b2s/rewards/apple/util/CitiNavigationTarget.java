package com.b2s.rewards.apple.util;

import com.b2s.security.saml.Throw;

/**
 * Created by ppalpandi on 11/27/2017.
 */
public enum CitiNavigationTarget {

    HOME("/citi/AppleStore.htm"),
    ORDER_HISTORY("/citi/ViewOrder.htm"),
    CONSENT("/citi/Consent.htm"),
    TERMS("/citi/Terms.htm");

    private final String path;

    CitiNavigationTarget(final String path){
        Throw.when("path",path).isEmptyString();
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
