package com.b2s.rewards.apple.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An implementation of the message builder that builds a multi-line message for the request and response.
 *
 * @author jkattookaren 6/20/2017
 */
public class MultiLineRequestResponseMessageBuilder implements RequestResponseMessageBuilder {

    @Override
    public String buildRequestMessage(
        final String name,
        final URI uri,
        final HttpMethod httpMethod,
        final HttpHeaders headers,
        final byte[] body) throws UnsupportedEncodingException {

        return Arrays.asList(
            String.format("=========================%s Request Begin=========================", name),
            String.format("URI         : %s", uri),
            String.format("Method      : %s", httpMethod),
            String.format("Headers     : %s", headers),
            String.format("Request body: %s", new String(body, "UTF-8")),
            String.format("=========================%s Request End===========================", name))
            .stream().collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public String buildResponseMessage(
        final String name,
        final HttpStatus statusCode,
        final String statusText,
        final HttpHeaders headers,
        final String body) {

        return Arrays.asList(
            String.format("=========================%s Response Begin=========================", name),
            String.format("Status code  : %s", statusCode),
            String.format("Status text  : %s", statusText),
            String.format("Headers      : %s", headers),
            String.format("Response body: %s", body),
            String.format("=========================%s Response End===========================", name))
            .stream().collect(Collectors.joining(System.lineSeparator()));
    }

}
