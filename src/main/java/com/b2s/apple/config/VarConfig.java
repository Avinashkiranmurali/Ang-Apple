package com.b2s.apple.config;

import com.b2s.rewards.apple.interceptor.LoggingRestRequestResponseInterceptor;
import com.b2s.rewards.apple.interceptor.OneLineRequestResponseMessageBuilder;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.shop.common.order.var.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Configuration
@ComponentScan(basePackages = {"com.b2s.shop.common.order.var", "com.b2s.common.services.awp"})
public class VarConfig {

    @Bean("varOrderManagerHolder")
    public VarOrderManagerHolder varOrderManagerHolder(
        @Autowired @Qualifier("varOrderManagerRBC") final VAROrderManagerRBC varOrderManagerRBC,
        @Autowired @Qualifier("varOrderManagerAmex") final VAROrderManagerAmex varOrderManagerAmex,
        @Autowired @Qualifier("varOrderManagerSCOTIA") final VAROrderManagerSCOTIA varOrderManagerSCOTIA,
        @Autowired @Qualifier("varOrderManagerVitalityUS") final VAROrderManagerVITALITYUS varOrderManagerVitalityUS,
        @Autowired @Qualifier("varOrderManagerUA") final VAROrderManagerUA varOrderManagerUA,
        @Autowired @Qualifier("varOrderManagerVitalityCA") final VAROrderManagerVITALITYCA varOrderManagerVitalityCA,
        @Autowired @Qualifier("varOrderManagerPNC") final VAROrderManagerPNC varOrderManagerPNC,
        @Autowired @Qualifier("varOrderManagerWF") final VAROrderManagerWF varOrderManagerWF,
        @Autowired @Qualifier("varOrderManagerCiti") final VAROrderManagerCiti varOrderManagerCiti,
        @Autowired @Qualifier("varOrderManagerDemo") final VAROrderManagerDemo varOrderManagerDemo,
        @Autowired @Qualifier("varOrderManagerDelta") final VAROrderManagerDelta varOrderManagerDelta,
        @Autowired @Qualifier("varOrderManagerChase") final VAROrderManagerChase varOrderManagerChase,
        @Autowired @Qualifier("varOrderManagerAmexAU") final VAROrderManagerAMEXAU varOrderManagerAmexAU,
        @Autowired @Qualifier("varOrderManagerFDR") final VAROrderManagerFDR varOrderManagerFDR,
        @Autowired @Qualifier("varOrderManagerFDR_PSCU") final VAROrderManagerFDR_PSCU varOrderManagerFDR_PSCU,
        @Autowired @Qualifier("varOrderManagerVirginAU") final VAROrderManagerVirginAU varOrderManagerVirginAU,
        @Autowired @Qualifier("varOrderManagerLAUSD") final VAROrderManagerLAUSD varOrderManagerLAUSD,
        @Autowired @Qualifier("varOrderManagerFSV") final VAROrderManagerFSV varOrderManagerFSV) {
        final VarOrderManagerHolder varOrderManagerHolder = new VarOrderManagerHolder();

        final Map<String, VAROrderManagerIF> varOrderManagerMap = new CaseInsensitiveMap();
        varOrderManagerMap.put("RBC", varOrderManagerRBC);
        varOrderManagerMap.put("Amex", varOrderManagerAmex);
        varOrderManagerMap.put("SCOTIA", varOrderManagerSCOTIA);
        varOrderManagerMap.put("VitalityUS", varOrderManagerVitalityUS);
        varOrderManagerMap.put("UA", varOrderManagerUA);
        varOrderManagerMap.put("VitalityCA", varOrderManagerVitalityCA);
        varOrderManagerMap.put("PNC", varOrderManagerPNC);
        varOrderManagerMap.put("WF", varOrderManagerWF);
        varOrderManagerMap.put("CITIGR", varOrderManagerCiti);
        varOrderManagerMap.put("AU", varOrderManagerCiti);
        varOrderManagerMap.put("SG", varOrderManagerCiti);
        varOrderManagerMap.put("MX", varOrderManagerCiti);
        varOrderManagerMap.put("HK", varOrderManagerCiti);
        varOrderManagerMap.put("MY", varOrderManagerCiti);
        varOrderManagerMap.put("TW", varOrderManagerCiti);
        varOrderManagerMap.put("PH", varOrderManagerCiti);
        varOrderManagerMap.put("AE", varOrderManagerCiti);
        varOrderManagerMap.put("TH", varOrderManagerCiti);
        varOrderManagerMap.put("Demo", varOrderManagerDemo);
        varOrderManagerMap.put("Delta", varOrderManagerDelta);
        varOrderManagerMap.put("Chase", varOrderManagerChase);
        varOrderManagerMap.put("AmexAU", varOrderManagerAmexAU);
        varOrderManagerMap.put("FDR", varOrderManagerFDR);
        varOrderManagerMap.put("FDR_PSCU", varOrderManagerFDR_PSCU);
        varOrderManagerMap.put("VirginAU", varOrderManagerVirginAU);
        varOrderManagerMap.put("LAUSD", varOrderManagerLAUSD);
        varOrderManagerMap.put("FSV", varOrderManagerFSV);
        varOrderManagerHolder.setVarOrderManagerMap(varOrderManagerMap);

        return varOrderManagerHolder;
    }

    @Bean("varConfigMap")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Map<String, String> varConfigMap() {
        final Map<String, String> varConfigMap = new HashMap<>();
        varConfigMap.put("citi", "CITIGR");
        return varConfigMap;
    }

    @Bean("httpClientUtil")
    public HttpClientUtil httpClientUtil(
        @Autowired @Qualifier("b2sRestTemplate") final RestTemplate b2sRestTemplate,
        @Autowired @Qualifier("applicationProperties") final Properties applicationProperties) {
        return new HttpClientUtil(b2sRestTemplate, applicationProperties);
    }

    @Bean("visHttpClientUtil")
    public HttpClientUtil visHttpClientUtil(
        @Autowired @Qualifier("visB2sRestTemplate") final RestTemplate b2sRestTemplate,
        @Autowired @Qualifier("applicationProperties") final Properties applicationProperties) {
        return new HttpClientUtil(b2sRestTemplate, applicationProperties);
    }

    @Bean("b2sRestTemplate")
    public RestTemplate b2sRestTemplate(
        @Autowired @Qualifier("bufferingClientHttpRequestFactory")
        final BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory,
        @Autowired @Qualifier("loggingInterceptor") final LoggingRestRequestResponseInterceptor loggingInterceptor,
        @Autowired @Qualifier("messageConverters") final MappingJackson2HttpMessageConverter messageConverter) {
        final RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(loggingInterceptor);
        restTemplate.setInterceptors(interceptors);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    @Bean("visB2sRestTemplate")
    public RestTemplate visB2sRestTemplate(
        @Autowired @Qualifier("bufferingClientHttpRequestFactory")
        final BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory,
        @Autowired @Qualifier("transactionIdInterceptor") final TransactionIdInterceptor transactionIdInterceptor,
        @Autowired @Qualifier("loggingInterceptor") final LoggingRestRequestResponseInterceptor loggingInterceptor,
        @Autowired @Qualifier("messageConverters") final MappingJackson2HttpMessageConverter messageConverter) {
        final RestTemplate restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(transactionIdInterceptor);
        interceptors.add(loggingInterceptor);
        restTemplate.setInterceptors(interceptors);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    @Bean("bufferingClientHttpRequestFactory")
    public BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory(
        @Autowired @Qualifier("httpComponentsClientHttpRequestFactory")
        final HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        return new BufferingClientHttpRequestFactory(httpRequestFactory);
    }

    @Bean("httpComponentsClientHttpRequestFactory")
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory(
        @Value("${http.connection.timeout}") final int readTimeOut,
        @Value("${http.connection.timeout}") final int connectTimeOut) {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(readTimeOut);
        httpRequestFactory.setConnectTimeout(connectTimeOut);
        return httpRequestFactory;
    }

    @Bean("loggingInterceptor")
    public LoggingRestRequestResponseInterceptor loggingInterceptor(
        @Autowired @Qualifier("oneLineReqRespMsgBuilder")
            OneLineRequestResponseMessageBuilder oneLineReqRespMsgBuilder) {
        return new LoggingRestRequestResponseInterceptor(oneLineReqRespMsgBuilder);
    }

    @Bean("oneLineReqRespMsgBuilder")
    public OneLineRequestResponseMessageBuilder oneLineReqRespMsgBuilder() {
        return new OneLineRequestResponseMessageBuilder();
    }

    @Bean("transactionIdInterceptor")
    public TransactionIdInterceptor transactionIdInterceptor() {
        return new TransactionIdInterceptor();
    }

    @Bean("messageConverters")
    public MappingJackson2HttpMessageConverter messageConverters(
        @Autowired @Qualifier("objectMapperForRest") final Jackson2ObjectMapperFactoryBean objectMapperForRest) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapperForRest.getObject());
        return converter;
    }

    @Bean("objectMapperForRest")   //Jackson2ObjectMapperFactoryBean
    public Jackson2ObjectMapperFactoryBean objectMapper() {
        final Jackson2ObjectMapperFactoryBean jackson2ObjectMapperFactoryBean = new Jackson2ObjectMapperFactoryBean();
        jackson2ObjectMapperFactoryBean.setModulesToInstall(Jdk8Module.class);
        return jackson2ObjectMapperFactoryBean;
    }
}
