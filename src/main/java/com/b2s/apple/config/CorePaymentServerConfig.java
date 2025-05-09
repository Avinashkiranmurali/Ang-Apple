package com.b2s.apple.config;

import com.b2r.paymentserver.api.PaymentServerClient;
import com.b2r.paymentserver.api.interceptor.RestTemplateInterceptor;
import com.b2r.paymentserver.api.service.APIResponseHandler;
import com.b2s.rewards.apple.interceptor.LoggingRestRequestResponseInterceptor;
import com.b2s.shop.common.order.var.TransactionIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorePaymentServerConfig {

    @Value("${payment.server.hmac.key}")
    private String paymentHMACKey;

    @Bean
    public PaymentServerClient getPaymentServerClient() {
        return new PaymentServerClient();
    }

    @Bean(name = "paymentHMACSecretKey")
    public SecretKeySpec getPaymentHMACSecretKey() {
        return new SecretKeySpec(paymentHMACKey.getBytes(), "HmacSHA256");
    }

    @Bean("restTemplateInterceptor")
    public RestTemplateInterceptor restTemplateInterceptor() {
        return new RestTemplateInterceptor();
    }

    @Bean("apiResponseHandler")
    public APIResponseHandler apiResponseHandler() {
        return new APIResponseHandler();
    }

    @Bean("paymentserverRestTemplate")
    public RestTemplate paymentserverRestTemplate(
            @Autowired @Qualifier("bufferingClientHttpRequestFactory")
            final BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory,
            @Autowired @Qualifier("transactionIdInterceptor") final TransactionIdInterceptor transactionIdInterceptor,
            @Autowired @Qualifier("loggingInterceptor") final LoggingRestRequestResponseInterceptor loggingInterceptor,
            @Autowired final RestTemplateInterceptor restTemplateInterceptor,
            @Autowired final APIResponseHandler apiResponseHandler,
            @Autowired @Qualifier("messageConverters") final MappingJackson2HttpMessageConverter messageConverter) {
        final RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);

        //Default interceptors for tracking and logging.
        //Note:The RestTemplateInterceptor needs to be last
        //as this injects the HMAC security key and we do not want that to be logged.
        final List<ClientHttpRequestInterceptor> interceptors =
                                    List.of(
                                        transactionIdInterceptor,
                                        loggingInterceptor,
                                        restTemplateInterceptor//<-- This needs to be the last
                                    );

        restTemplate.setInterceptors(interceptors);

        //Error Handler
        restTemplate.setErrorHandler(apiResponseHandler);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }
}
