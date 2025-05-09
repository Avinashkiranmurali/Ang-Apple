package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

import java.util.Map;

/**
 * Created by skither on 9/12/2018.
 */
public class UserChase extends User {

    private String sessionState;
    private Map<String, String> analyticsWindow;
    private String analyticsUrl;

    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }

    public Map<String, String> getAnalyticsWindow() {
        return analyticsWindow;
    }

    public void setAnalyticsWindow(final Map<String, String> analyticsWindow) {
        this.analyticsWindow = analyticsWindow;
    }

    public String getAnalyticsUrl() {
        return analyticsUrl;
    }

    public void setAnalyticsUrl(final String analyticsUrl) {
        this.analyticsUrl = analyticsUrl;
    }
}
