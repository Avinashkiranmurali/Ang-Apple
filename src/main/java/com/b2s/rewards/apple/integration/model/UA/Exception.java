package com.b2s.rewards.apple.integration.model.UA;

/**
 * Created by srukmagathan on 28-08-2018.
 */
public class Exception {

    private String exceptionLocation;
    private String exceptionCode;
    private String exceptionMessage;

    public String getExceptionLocation() {
        return exceptionLocation;
    }

    public void setExceptionLocation(final String exceptionLocation) {
        this.exceptionLocation = exceptionLocation;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(final String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(final String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
