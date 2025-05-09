package com.b2s.rewards.apple.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * An extensible way to build request and response messages.
 *
 * @author jkattookaren 6/20/2017
 */
public interface RequestResponseMessageBuilder {

    /**
     * Build the request message.
     *
     * @param name a distinguishing name for the request
     * @param uri the request uri
     * @param httpMethod the request http method
     * @param headers the request headers
     * @param body the request body
     * @return the request message built from the parameters
     * @throws UnsupportedEncodingException
     */
    String buildRequestMessage(
        String name,
        URI uri,
        HttpMethod httpMethod,
        HttpHeaders headers,
        byte[] body) throws UnsupportedEncodingException;

    /**
     * Build the response message.
     *
     * @param name a distinguishing name for the response
     * @param statusCode the response status code
     * @param statusText the response status text
     * @param headers the response headers
     * @param body the response body
     * @return the response message built from the parameters
     */
    String buildResponseMessage(
        String name,
        HttpStatus statusCode,
        String statusText,
        HttpHeaders headers,
        String body);

}
