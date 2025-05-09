package com.b2s.rewards.apple.model;

import java.io.Serializable;
/**
 * Created by ssundaramoorthy on 2/19/2021
 **/
public class Five9Config implements Serializable {
    private static final long serialVersionUID = 3692476882854601918L;
    private Boolean chatEnable;
    private String rootUrl;
    private String type;
    private String tenant;
    private String profile;
    private String title;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChatEnable(Boolean chatEnable) {
        this.chatEnable = chatEnable;
    }

    public Boolean isChatEnabled() {
        return chatEnable;
    }
}