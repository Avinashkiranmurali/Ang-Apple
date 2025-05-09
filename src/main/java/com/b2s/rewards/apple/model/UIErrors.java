package com.b2s.rewards.apple.model;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by ssrinivasan on 3/25/2015.
 * Used to receive the UI errors to be logged on the server side in LoggingController
 */
public class UIErrors {

    private String errorUrl;
    private String errorMessage;
    private List<String> stackTrace;
    private String cause;
    private String browserInfo;

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getBrowserInfo() {
        return browserInfo;
    }

    public void setBrowserInfo(String browserInfo) {
        this.browserInfo = browserInfo;
    }

    @Override
    /**
     * This method is used to write to the log, any kind of formatting should happen here.
     */
    public String toString() {
        return "UIErrors [errorUrl=" + errorUrl + ", errorMessage="
                + errorMessage +  ", cause="
                + cause + ", browserInfo=" + browserInfo +", stackTrace=" + StringUtils.join(stackTrace,'\n')+ "]";
    }}
