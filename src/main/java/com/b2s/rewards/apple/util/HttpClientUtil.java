package com.b2s.rewards.apple.util;

import com.b2s.rewards.common.exception.B2RException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.gsonToJsonString;

/**
 * Created by rpillai on 7/11/2016.
 */
public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);
    public static final String ERROR_RESPONSE_URL_AND_RESPONSE_CLASS =
        "Error while getting response for the url {} {} and response class {}";
    public static final String ERROR_WHILE_GETTING_RESPONSE_FOR_THE_URL_WITH_EXCEPTION =
        "Error while getting response for the url {} {} with exception {}";

    private RestOperations b2sRestTemplate;
    private Properties applicationProperties;

    public HttpClientUtil(RestOperations restOperations, Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.b2sRestTemplate = restOperations;
    }

    public <T, R> T getHttpResponse(String url, Class<T> resultClass, HttpMethod method, R request) throws B2RException {
        T response = null;
        ResponseEntity<T> responseEntity = null;
        try {

            switch (method) {
                case GET:
                    responseEntity = b2sRestTemplate.getForEntity(url, resultClass);
                    break;
                case POST:
                    responseEntity = b2sRestTemplate.postForEntity(url, request, resultClass);
                    break;
                case PUT:
                    HttpEntity<R> entity = new HttpEntity<R>(request);
                    responseEntity = b2sRestTemplate.exchange(url, HttpMethod.PUT, entity, resultClass);
                    break;
                default:
                    LOG.error("Not supported method: {}", method);
                    break;
            }
            if (responseEntity != null) {
                if (responseEntity.getStatusCode() != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    response = responseEntity.getBody();
                } else {
                    LOG.error(ERROR_RESPONSE_URL_AND_RESPONSE_CLASS, method, url, resultClass);
                    throw new B2RException((String) responseEntity.getBody());
                }
            } else {
                LOG.error(ERROR_RESPONSE_URL_AND_RESPONSE_CLASS, method, url, resultClass);
                throw new B2RException("Error while getting response for the url");
            }
        } catch (Exception e) {
            LOG.error(ERROR_WHILE_GETTING_RESPONSE_FOR_THE_URL_WITH_EXCEPTION, method, url, e);
            if (e != null && e instanceof HttpClientErrorException && StringUtils.isNotBlank(((HttpClientErrorException) e).getResponseBodyAsString())) {
                throw new B2RException(((HttpClientErrorException) e).getResponseBodyAsString());
            }
            if (e != null && e instanceof HttpServerErrorException && StringUtils.isNotBlank(((HttpServerErrorException) e).getResponseBodyAsString())) {
                throw new B2RException(((HttpServerErrorException) e).getResponseBodyAsString());
            }
            throw new B2RException(e.getMessage());
        }
        return response;
    }

    public <T, R> ResponseEntity<T> getHttpResponse(String url, Class<T> resultClass, HttpMethod method, R request, Map<String, String> headers, boolean isForm) {
        ResponseEntity<T> responseEntity = null;
        String gsonToJsonString = null;
        HttpEntity<R> entity = null;
        if (request != null) {
            gsonToJsonString = gsonToJsonString(request);
            LOG.info("Request data for the url {} {}: {}", method, url, gsonToJsonString);
            MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
            headerMap.setAll(headers);
            entity = new HttpEntity(request, headerMap);
        }

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        try {
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectTimeout(new Integer(applicationProperties.getProperty("http.connection.timeout")));
            requestFactory.setReadTimeout(new Integer(applicationProperties.getProperty("http.connection.timeout")));

            RestTemplate restTemplate = new RestTemplate(requestFactory);

            if (!isForm) {
                List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
                messageConverters.add(new GsonHttpMessageConverter());
                restTemplate.setMessageConverters(messageConverters);
            }

            switch (method) {
                case GET:
                    responseEntity = restTemplate.getForEntity(url, resultClass);
                    break;
                case POST:
                    responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, resultClass);
                    break;
                case PUT:
                    responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, resultClass);
                    break;
                default:
                    LOG.error("Not supported method: {}", method);
                    break;
            }
            if (responseEntity != null && responseEntity.getStatusCode() != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                gsonToJsonString = gsonToJsonString(responseEntity.getBody());
                LOG.info("Got successful response for the url {} {}. Response: {}", method, url, gsonToJsonString);
            } else {
                LOG.error(ERROR_RESPONSE_URL_AND_RESPONSE_CLASS, method, url, resultClass);
            }
        } catch (Exception e) {
            LOG.error(ERROR_WHILE_GETTING_RESPONSE_FOR_THE_URL_WITH_EXCEPTION, method, url, e);
        }
        return responseEntity;
    }

    public <T, R> T getHttpResponseWithHeaders(final String url, final Class<T> resultClass, final HttpMethod method, final R request, final HttpHeaders headers) throws B2RException {
        T response = null;
        ResponseEntity<T> responseEntity = null;
        String gsonToJsonString = null;
        if (request != null) {
            gsonToJsonString = gsonToJsonString(request);
            LOG.info("Request data for the url {} {}: {}", method, url, gsonToJsonString);
        }
        try {

            final HttpEntity<R> entity = new HttpEntity<R>(request, headers);

            responseEntity = b2sRestTemplate.exchange(url, method, entity, resultClass);

            if (responseEntity != null && responseEntity.getStatusCode() != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                response = responseEntity.getBody();
                gsonToJsonString = gsonToJsonString(response);
                LOG.info("Got successful response for the url {} {}. Response: {}", method, url, gsonToJsonString);
            } else {
                LOG.error(ERROR_RESPONSE_URL_AND_RESPONSE_CLASS, method, url, resultClass);
                throw new B2RException((String) responseEntity.getBody());
            }
        } catch (final Exception e) {
            LOG.error(ERROR_WHILE_GETTING_RESPONSE_FOR_THE_URL_WITH_EXCEPTION, method, url, e);
            if (e != null && e instanceof HttpClientErrorException && StringUtils.isNotBlank(((HttpClientErrorException) e).getResponseBodyAsString())) {
                throw new B2RException(((HttpClientErrorException) e).getResponseBodyAsString());
            }
            throw new B2RException(e.getMessage());
        }
        return response;
    }

}
