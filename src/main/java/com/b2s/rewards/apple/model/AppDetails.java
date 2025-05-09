package com.b2s.rewards.apple.model;

/**
 * Created by vkrishnan on 4/21/2019.
 */
public class AppDetails {
    private String applicationName;
    private AppVersion applicationInfo;


    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public AppVersion getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(final AppVersion applicationInfo) {
        this.applicationInfo = applicationInfo;
    }
}
