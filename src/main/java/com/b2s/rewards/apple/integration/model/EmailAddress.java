package com.b2s.rewards.apple.integration.model;

/**
 * Created by rperumal on 8/28/2015.
 */
public class EmailAddress {
    private String email;
    private String type;  //TODO: Enum

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
