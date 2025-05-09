package com.b2s.apple.config;

import com.b2s.rewards.apple.interceptor.LoggingRestRequestResponseInterceptor;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.shop.common.order.var.TransactionIdInterceptor;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.HTTP_CONNECTION_TIMEOUT;

/**
 *  Creates HttpClientUtil or HCU objects with different timeout values.
 */
@Component("httpClientUtilFactory")
@DependsOn("varOrderManagerHolder")
public class HttpClientUtilFactory {

    @SuppressWarnings("unchecked")
    static private Map<String, HttpClientUtil> varIdHCUMap = new CaseInsensitiveMap();

    @SuppressWarnings("unchecked")
    static private Map<String, Integer> varIdTimeoutMap = new CaseInsensitiveMap();

    @Autowired
    private VarOrderManagerHolder varOrderManagerHolder;

    @Autowired @Qualifier("applicationProperties")
    private Properties applicationProperties;

    @Autowired 
    private HttpClientUtil visHttpClientUtil;

    @Autowired @Qualifier("transactionIdInterceptor")
    private TransactionIdInterceptor transactionIdInterceptor;

    @Autowired @Qualifier("loggingInterceptor")
    private LoggingRestRequestResponseInterceptor loggingInterceptor;

    @Autowired @Qualifier("messageConverters")
    private MappingJackson2HttpMessageConverter messageConverter;

    /**
     * Setting up the HashMap varIdHCUMap. Each VAR will have HttpClientUtil object.
     * Most of them will share the default bean visHttpClientUtil.
     * If the timeout value is different for a var, it will have its own HttpClientUtil.
     * At the time of writing this code, UA has a larger timeout value in UAT than default.
     *
     * This class is marked as Component so Spring will create the static HashMaps at startup and not injected anywhere.
     * The auto-wiring of HttpClientUtil in VarIntegrationServiceRemoteImpl has been removed.
     *
     * To avoid circular dependency of bean creation, the functions are marked static and invoked at runtime.
     *
     */
    @PostConstruct
    private void setup()
    {
        @SuppressWarnings("unchecked")
        Map<Integer, HttpClientUtil> timeoutHCUMap = new HashedMap();

        int defaultTimeoutValue = Integer.parseInt((String)applicationProperties.get(HTTP_CONNECTION_TIMEOUT));
        timeoutHCUMap.put(defaultTimeoutValue, visHttpClientUtil);

        int intTimeout;

        Set<String> varIdSet = varOrderManagerHolder.getVarOrderManagerMap().keySet();
        for (String varId : varIdSet) {
            String varTimeout = varId + "." + HTTP_CONNECTION_TIMEOUT;
            try {
                intTimeout = Integer.parseInt((String)applicationProperties.get(varTimeout));
            } catch (NumberFormatException ignore) {
                // varId does not have its own timeout value, use default.
                varIdHCUMap.put(varId, visHttpClientUtil);
                varIdTimeoutMap.put(varId, defaultTimeoutValue);
                continue;
            }
            if(timeoutHCUMap.containsKey(intTimeout)) {
                varIdHCUMap.put(varId, timeoutHCUMap.get(intTimeout));
            } else {
                HttpClientUtil httpClientUtil = createHttpClientUtil(intTimeout);
                timeoutHCUMap.put(intTimeout, httpClientUtil);
                varIdHCUMap.put(varId, httpClientUtil);
            }
            varIdTimeoutMap.put(varId, intTimeout);
        }
    }

    private HttpClientUtil createHttpClientUtil(int timeout) {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(timeout);
        httpRequestFactory.setConnectTimeout(timeout);

        final BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory =
                new BufferingClientHttpRequestFactory(httpRequestFactory);

        final RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(transactionIdInterceptor);
        interceptors.add(loggingInterceptor);
        restTemplate.setInterceptors(interceptors);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
        return new HttpClientUtil(restTemplate, applicationProperties);
    }

    static public HttpClientUtil getCustomHttpClientUtil(String varId)
    {
        return varIdHCUMap.get(varId);
    }

    static public int getTimeoutValue(String varId)
    {
        Integer i = varIdTimeoutMap.get(varId);
        if(i != null) {
            return i;
        }
        return 0;
    }
}
