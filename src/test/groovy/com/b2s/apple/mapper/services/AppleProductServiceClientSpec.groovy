package com.b2s.apple.mapper.services

import com.b2s.apple.model.SubscriptionResponseDTO
import com.b2s.apple.services.AppleProductServiceClient
import com.b2s.rewards.apple.util.HttpClientUtil
import com.b2s.rewards.common.exception.B2RException
import com.b2s.rewards.common.util.CommonConstants
import spock.lang.Specification
import spock.lang.Subject

import java.time.OffsetDateTime

class AppleProductServiceClientSpec extends Specification {

    def httpClient = Mock(HttpClientUtil)
    def applicationProperties = Mock(Properties)

    @Subject
    def appleProductServiceClient = new AppleProductServiceClient(httpClient: httpClient, applicationProperties: applicationProperties)

    def "test getSubscriptionInfo OK"() {
        setup:
        applicationProperties.getProperty(CommonConstants.PS3_HTTP_URL) >> "http://ps-vip.apexaqa1.bridge2solutions.net:8080/"
        httpClient.getHttpResponse(_, _, _, _) >> getMockResponse(subscriptionType)
        def subscriptionResponse = appleProductServiceClient.getSubscriptionInfo(subscriptionType, language)

        expect:
        subscriptionResponse != null
        subscriptionResponse.expirationDateTime.isPresent()
        subscriptionResponse.remainingCount.isPresent()
        subscriptionResponse.redemptionUrl != null
        subscriptionResponse.getRedemptionUrl() == redemptionUrl

        where:
        subscriptionType | language  || redemptionUrl
        'amp-news-plus'  | 'English' || 'applenews:/redeem?ctx=News&code=DQ6WH7Q4EZ7G'
        'amp-music'      | 'English' || 'https://music.apple.com/redeem?ctx=Music&code=63Y3JDAHLDFD'
        'amp-tv-plus'    | 'English' || 'https://tv.apple.com/redeem?ctx=tv&code=22A6RTD3LUZG'
    }

    def "test getSubscriptionInfo with Exception"() {
        setup:
        applicationProperties.getProperty(CommonConstants.PS3_HTTP_URL) >> "http://ps-vip.apexaqa1.bridge2solutions.net:8080/"
        httpClient.getHttpResponse(_, _, _, _) >> {
            throw new B2RException(getErrorResponse())
        }

        when:
        def subscriptionResponse = appleProductServiceClient.getSubscriptionInfo("amp-news-plus", "English")

        then:
        subscriptionResponse == null
    }

    String getErrorResponse() {
        return "{\"type\":\"NOT_FOUND\",\"message\":\"subscriptionType is not valid: amp-tv-plus\",\"timestamp\":\"2022-02-23T05:33:22.894643-05:00\"}"
    }

    SubscriptionResponseDTO getMockResponse(String subscriptionType) {
        String redemptionUrl = null;
        if (subscriptionType == 'amp-news-plus') {
            redemptionUrl = 'applenews:/redeem?ctx=News&code=DQ6WH7Q4EZ7G'
        } else if (subscriptionType == 'amp-music') {
            redemptionUrl = 'https://music.apple.com/redeem?ctx=Music&code=63Y3JDAHLDFD'
        } else if (subscriptionType == 'amp-tv-plus') {
            redemptionUrl = 'https://tv.apple.com/redeem?ctx=tv&code=22A6RTD3LUZG'
        }
        return SubscriptionResponseDTO.builder()
                .withRedemptionUrl(redemptionUrl)
                .withRemainingCount(10)
                .withExpirationDateTime(OffsetDateTime.now())
                .build();
    }
}
