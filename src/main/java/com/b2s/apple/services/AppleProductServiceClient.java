package com.b2s.apple.services;

import com.b2s.apple.model.ErrorResponseDTO;
import com.b2s.apple.model.SubscriptionRequestDTO;
import com.b2s.apple.model.SubscriptionResponseDTO;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class AppleProductServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AppleProductServiceClient.class);
    private static final String PS3_HTTP_URL = "PS3_HTTP_URL";
    private static final String PATH_SUBSCRIPTION = "/subscription";
    private static final String PATH_INFO = "/info";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    @Qualifier("httpClientUtil")
    private HttpClientUtil httpClient;

    public SubscriptionResponseDTO getSubscriptionInfo(final String subscriptionType, final String language) {
        SubscriptionResponseDTO subscriptionResponse = null;

        final StringBuilder productServiceUrl = new StringBuilder(applicationProperties.getProperty(PS3_HTTP_URL))
            .append(PATH_SUBSCRIPTION)
            .append(PATH_INFO);

        final SubscriptionRequestDTO subscriptionRequest = SubscriptionRequestDTO.builder()
            .withSubscriptionType(subscriptionType)
            .withLanguage(language)
            .withMerchant(CommonConstants.APPLE)
            .build();

        try {
            subscriptionResponse = httpClient
                .getHttpResponse(productServiceUrl.toString(), SubscriptionResponseDTO.class, HttpMethod.POST,
                    subscriptionRequest);
        } catch (final B2RException be) {
            logger.error("Unable to send AMP Subscription Email: Error Response from PS(POST-SubscriptionInfo): {}",
                be.getMessage());
        }
        return subscriptionResponse;
    }
}
