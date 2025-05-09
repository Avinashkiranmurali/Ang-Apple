package com.b2s.rewards.apple.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * An implementation of the message builder that builds a one-line message for the request and response.
 *
 * @author jkattookaren 6/20/2017
 */
public class OneLineRequestResponseMessageBuilder implements RequestResponseMessageBuilder {

    @Override
    public String buildRequestMessage(
        final String name,
        final URI uri,
        final HttpMethod httpMethod,
        final HttpHeaders headers,
        final byte[] body) throws UnsupportedEncodingException {

        return String.format("%s Request: %s %s [Headers: %s] %s",
            name, httpMethod, uri, headers, new String(body, "UTF-8"));
    }

    @Override
    public String buildResponseMessage(
        final String name,
        final HttpStatus statusCode,
        final String statusText,
        final HttpHeaders headers,
        final String body) {

        return String.format("%s Response: %s %s [Headers: %s] %s", name, statusCode, statusText, headers, body);
    }

}
