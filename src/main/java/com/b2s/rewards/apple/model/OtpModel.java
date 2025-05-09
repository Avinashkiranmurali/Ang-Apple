package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*** Created by srukmagathan on 8/31/2016.
 */
public class OtpModel {

    private String email;

    @JsonIgnore
    private String overrideEmail;

    private String password;

    private String locale;

    public String getLocale() {
        return locale;
    }

    public OtpModel setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getOverrideEmail() {
        return overrideEmail;
    }

    public void setOverrideEmail(String overrideEmail) {
        this.overrideEmail = overrideEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
