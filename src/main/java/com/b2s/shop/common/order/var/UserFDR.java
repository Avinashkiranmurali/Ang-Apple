package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

public class UserFDR extends User {

    private String sessionState;

    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }
}

