package com.b2s.rewards.apple.integration.model.lausd;

import java.io.Serializable;

public class ParentInfo implements Serializable {

    private static final long serialVersionUID = -3937940452517255110L;
    private String parentEmail;
    private String parentFirstName;
    private String parentLastName;

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(final String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getParentFirstName() {
        return parentFirstName;
    }

    public void setParentFirstName(final String parentFirstName) {
        this.parentFirstName = parentFirstName;
    }

    public String getParentLastName() {
        return parentLastName;
    }

    public void setParentLastName(final String parentLastName) {
        this.parentLastName = parentLastName;
    }
}
