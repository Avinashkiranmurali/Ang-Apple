package com.b2s.apple.services;

import com.b2s.apple.model.HeapAnalyticsRequest;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.constant.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.b2s.apple.util.AnalyticsConstant.*;

@Service
public class HeapAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(HeapAnalyticsService.class);

    @Autowired
    @Qualifier("httpClientUtil")
    private HttpClientUtil httpClient;

    @Autowired
    private Properties applicationProperties;

    public void trackEvent(final Order order, final Program program, final User user, AnalyticsService.AnalyticsEventName eventName) {
        switch (eventName) {
            case ORDER_CANCELLATION:
                trackOrderCancelEvent(order, program, user);
                break;
            default: break;
        }

    }

    private void trackOrderCancelEvent(final Order order, final Program program, final User user) {
        order.getOrderLines().parallelStream()
                .forEach(orderLine ->
                        CompletableFuture.supplyAsync(() ->
                                processOrderLineCancelEvent(orderLine, program, user))
                );
    }

    private String processOrderLineCancelEvent(final OrderLine orderLine, final Program program, final User user) {
        try {
            logger.info("HeapAnalyticsService - processing Order Line Cancellation event {} - {}", orderLine.getOrderId(), orderLine.getLineNum());
            HeapAnalyticsRequest request = HeapAnalyticsRequest.builder()
                    .withAppId(AppleUtil.getProgramConfigValueAsString(program, CommonConstants.ANALYTICS_HEAP_APPID))
                    .withEvent(HEAP_ANALYTICS_ORDER_CANCEL_EVENT_NAME)
                    .withIdentity(user.getHashedUserId())
                    .withProperties(buildOrderCancelProperties(orderLine, program, user))
                    .build();
            httpClient.getHttpResponseWithHeaders(applicationProperties.getProperty(CommonConstants.HEAP_API_ENDPOINT), Void.class, HttpMethod.POST, request, createHeaders());
            logger.info("HeapAnalyticsService - Event processed successfully");
            return Constant.SUCCESS;
        } catch (B2RException e) {
            logger.error("Heap analytics request failed ", e);
            return null;
        }
    }

    private Map<String, Object> buildOrderCancelProperties(final OrderLine orderLine, final Program program, final User user) {
        final Map<String, Object> properties = buildCommonProperties(orderLine, program, user);

        properties.put(HEAP_ANALYTICS_ORDER_ID, orderLine.getOrderId());
        properties.put(HEAP_ANALYTICS_ORDER_LINE_NUM, orderLine.getLineNum());
        properties.put(HEAP_ANALYTICS_ORDER_LINE_QUANTITY, orderLine.getQuantity());
        properties.put(HEAP_ANALYTICS_ORDER_LINE_TOTAL,
                AppleUtil.getOrderLineAttributeValue(orderLine, CommonConstants.ORDER_ATTR_KEY_DISPLAY_TOTAL_PRICE_AMOUNT));
        properties.put(HEAP_ANALYTICS_ORDER_LINE_TOTAL_POINTS, orderLine.getOrderLinePoints());

        properties.put(HEAP_ANALYTICS_ORDER_LINE_ITEM_PRICE,
                AppleUtil.getOrderLineAttributeValue(orderLine,CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT));

        properties.put(HEAP_ANALYTICS_ORDER_LINE_ITEM_POINTS,
                AppleUtil.getOrderLineAttributeValue(orderLine, CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_POINTS));

        properties.put(HEAP_ANALYTICS_ITEM_SKU, orderLine.getSku());
        properties.put(HEAP_ANALYTICS_ITEM_NAME, orderLine.getName());
        properties.put(HEAP_ANALYTICS_ITEM_CATEGORY, orderLine.getCategory());
        properties.put(HEAP_ANALYTICS_ITEM_BRAND, orderLine.getBrand());
        properties.put(HEAP_ANALYTICS_ITEM_SUPPLIER, ANALYTICS_APPLE_STR);

        return properties;
    }

    private Map<String, Object> buildCommonProperties(final OrderLine orderLine, final Program program, final User user) {
        final Map<String, Object> properties = new HashMap<>();

        properties.put(COMMON_ANALYTICS_VAR_ID, orderLine.getVarId());
        properties.put(COMMON_ANALYTICS_VAR_NAME, orderLine.getVarId());
        properties.put(COMMON_ANALYTICS_PROGRAM_ID, program.getProgramId());
        properties.put(COMMON_ANALYTICS_PROGRAM_NAME, program.getName());
        properties.put(COMMON_ANALYTICS_PLATFORM, ANALYTICS_APPLE_STR);
        properties.put(COMMON_ANALYTICS_STOREFRONT_TYPE, ANALYTICS_MERCHANDISE_STR);
        properties.put(COMMON_ANALYTICS_STOREFRONT_NAME, ANALYTICS_APPLE_STR);
        properties.put(COMMON_ANALYTICS_ON_BEHALF_OF, StringUtils.isNotBlank(user.getProxyUserId()));
        properties.put(COMMON_ANALYTICS_EVENT_SOURCE, ANALYTICS_STOREFRONT_STR); // "storefront" for webapp. "admin" for Admin

        return properties;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CommonConstants.HTTP_HEADER_CONTENT_TYPE, CommonConstants.APPLICATION_JSON);
        httpHeaders.set(CommonConstants.HTTP_HEADER_ACCPT, CommonConstants.APPLICATION_JSON);
        return httpHeaders;
    }
}
