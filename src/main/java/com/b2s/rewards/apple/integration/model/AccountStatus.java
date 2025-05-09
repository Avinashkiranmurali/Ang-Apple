package com.b2s.rewards.apple.integration.model;

/**
 * @author rperumal
 */
public class AccountStatus {

    private String accessType;
    private String statusCode;  //TODO:  make sure that this gets value from StatusCode enum
    private String statusMessage;
    private Object varStatusInfo;

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Object getVarStatusInfo() {
        return varStatusInfo;
    }

    public void setVarStatusInfo(Object varStatusInfo) {
        this.varStatusInfo = varStatusInfo;
    }
}