package com.b2s.rewards.apple.interceptor;

/**
 * {@link AbstractLoggingHttpRequestResponseInterceptor} to log the rest request and response.
 *
 * @author jkattookaren 5/5/2017
 * @since 1.41.0
 */
public class LoggingRestRequestResponseInterceptor extends AbstractLoggingHttpRequestResponseInterceptor {

    private static final String REQUEST_NAME_SIGNIFIER_REST = "Rest";

    public LoggingRestRequestResponseInterceptor(final RequestResponseMessageBuilder messageBuilder) {
        super(messageBuilder, REQUEST_NAME_SIGNIFIER_REST);
    }

}
