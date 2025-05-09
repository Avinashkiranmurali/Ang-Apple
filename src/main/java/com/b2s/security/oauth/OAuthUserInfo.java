package com.b2s.security.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthUserInfo {

    @JsonProperty("sub")
    private String sub;
    @JsonProperty("tier")
    private String tier;
    @JsonProperty("name")
    private String name;
    @JsonProperty("preferred_username")
    private String preferredUsername;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    @JsonProperty("email")
    private String email;

    public String getSub() {
        return sub;
    }

    public void setSub(final String sub) {
        this.sub = sub;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(final String tier) {
        this.tier = tier;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(final String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(final String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
