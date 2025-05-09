package com.b2s.apple.services;

import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.mapper.ApiObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

public abstract class AbstractCartPricingRestApi {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    private final RestTemplate restTemplate;

    protected AbstractCartPricingRestApi() {
        this(getConnectTimeout(),getReadTimeout());
    }

    protected AbstractCartPricingRestApi(final int connectTimeout, final int readTimeout) {

        final HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
            new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setConnectTimeout(connectTimeout);
        httpComponentsClientHttpRequestFactory.setReadTimeout(readTimeout);

        final MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(ApiObjectMapper.get());

        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(ImmutableList.of(messageConverter));
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static int getConnectTimeout(){
        try{
            return Integer.parseInt(getProperties().getProperty(CommonConstants.PRICING_SERVICE_CONNECTION_TIMEOUT));
        }catch (Exception ex){
            return DEFAULT_CONNECT_TIMEOUT;
        }
    }

    public static int getReadTimeout(){
        try{
            return Integer.parseInt(getProperties().getProperty(CommonConstants.PRICING_SERVICE_READ_TIMEOUT));
        }catch (Exception ex){
            return DEFAULT_READ_TIMEOUT;
        }
    }

    public static Properties getProperties(){
        return (Properties) AppContext.getApplicationContext().getBean(CommonConstants.APPLICATION_PROPERTIES);
    }
}