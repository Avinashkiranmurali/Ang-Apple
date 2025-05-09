package com.b2s.rewards.apple.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link ClientHttpRequestInterceptor} to log the rest request and response.
 *
 * @author jkattookaren 6/19/2017
 */
public abstract class AbstractLoggingHttpRequestResponseInterceptor implements ClientHttpRequestInterceptor {

    @SuppressWarnings("NonConstantLogger")
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final RequestResponseMessageBuilder messageBuilder;
    protected final String name;

    protected AbstractLoggingHttpRequestResponseInterceptor(
        final RequestResponseMessageBuilder messageBuilder, final String name) {

        this.name = name;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public ClientHttpResponse intercept(
        final HttpRequest request,
        final byte[] body,
        final ClientHttpRequestExecution execution) throws IOException {

        traceRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private void traceRequest(final HttpRequest request, final byte[] body) throws IOException {
        final URI uri = request.getURI();
        final HttpMethod httpMethod = request.getMethod();
        final HttpHeaders headers = request.getHeaders();
        final String message = messageBuilder.buildRequestMessage(name, uri, httpMethod, headers, body);

        logger.info(message);
    }

    private void traceResponse(final ClientHttpResponse response) throws IOException {
        final HttpStatus statusCode = response.getStatusCode();
        final String statusText = response.getStatusText();
        final HttpHeaders headers = response.getHeaders();
        final String body = readResponseBody(response);
        final String message = messageBuilder.buildResponseMessage(name, statusCode, statusText, headers, body);

        logger.info(message);
    }

    private String readResponseBody(final ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError()) {
            return null;
        }

        if (Optional.ofNullable(response.getBody()).isEmpty()) {
            return null;
        }

        try (final BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(response.getBody(), "UTF-8"))) {
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

}
