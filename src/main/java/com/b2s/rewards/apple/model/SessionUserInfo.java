package com.b2s.rewards.apple.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class SessionUserInfo implements Serializable {

    private static final long serialVersionUID = -9202457687192875681L;

    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return (StringUtils.isEmpty(this.getFirstName()) ? "" : this.getFirstName().trim()) + " " +
                (StringUtils.isEmpty(this.getLastName()) ? "" : this.getLastName().trim());
    }

    @Override
    public String toString() {
        return "SessionUserInfo{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
