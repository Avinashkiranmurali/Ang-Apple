package com.b2s.web.Errors;

public class ErrorInfo {
    public final String url;
    public final String ex;

    public ErrorInfo(final String url, final Exception ex) {
        this.url = url;
        this.ex = ex.getLocalizedMessage();
    }
}