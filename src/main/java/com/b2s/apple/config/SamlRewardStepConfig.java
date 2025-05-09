package com.b2s.apple.config;

import com.b2s.apple.services.SAMLSessionService;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.filter.AppleSAMLProtectionFilter;
import com.b2s.security.saml.BuildEpsilonSAMLResponse;
import com.b2s.security.saml.BuildEpsilonSAMLResponseFromString;
import com.b2s.security.saml.XMLDecrypter;
import com.b2s.security.saml.config.SAMLConfiguration;
import com.b2s.security.saml.controller.SAMLResponseController;
import com.b2s.security.saml.security.SAMLAssertionEncrypter;
import com.b2s.security.saml.security.SAMLKeyStoreFactoryBean;
import com.b2s.security.saml.security.SAMLObjectSigner;
import com.b2s.security.saml.service.ProtectionDeciderService;
import com.b2s.security.saml.service.SAMLAuthnRequestBuilder;
import com.b2s.security.saml.service.SAMLResponseProcessor;
import com.b2s.security.saml.service.impl.RefererBasedPDS;
import com.b2s.security.saml.service.impl.SAMLConfigurationPDS;
import com.b2s.security.saml.util.SAMLDateTimeSkewValidator;
import com.b2s.security.saml.util.SAMLObjectFactory;
import com.b2s.security.saml.util.SecurityIntention;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
public class SamlRewardStepConfig extends WebMvcConfigurationSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRewardStepConfig.class);

    @Override //added to overcome pricing client json/xml conversion issue
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper().getObject());
        messageConverters.add(converter);
        super.addDefaultHttpMessageConverters(messageConverters);
    }

    @Bean("objectMapper")   //Jackson2ObjectMapperFactoryBean
    public Jackson2ObjectMapperFactoryBean objectMapper() {
        final Jackson2ObjectMapperFactoryBean jackson2ObjectMapperFactoryBean = new Jackson2ObjectMapperFactoryBean();
        jackson2ObjectMapperFactoryBean.setModulesToInstall(Jdk8Module.class);
        return jackson2ObjectMapperFactoryBean;
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        final Resource applicationResource = new ClassPathResource("environment.properties");
        final Resource environmentResource = new ClassPathResource("image-proxy-client.properties");
        final Resource imageProxyResource = new ClassPathResource("application.properties");

        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
            new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(applicationResource, environmentResource, imageProxyResource);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertySourcesPlaceholderConfigurer.setFileEncoding("UTF-8");
        propertySourcesPlaceholderConfigurer.setOrder(10000000);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean("protectionDeciderServiceReferBasedPDS")
    public RefererBasedPDS protectionDeciderServiceReferBasedPDS() {
        final RefererBasedPDS refererBasedPDS = new RefererBasedPDS("aplgr");
        return refererBasedPDS;
    }

    @Bean(name = "samlVelocityEngine", initMethod = "init")
    public VelocityEngine samlVelocityEngine(
        @Autowired @Qualifier("velocityProperties") final Properties velocityProperties) {
        VelocityEngine velocityEngine = null;
        try {
            velocityEngine = new VelocityEngine(velocityProperties);
        } catch (Exception ex) {
            LOGGER.error("ServiceException occurred in samlVelocityEngine. Error Message: ", ex);
        }
        return velocityEngine;
    }

    @Bean("velocityProperties")
    public Properties velocityProperties() {
        final Properties velocityProperties = new Properties();
        velocityProperties.setProperty("runtime.log", "${catalina.base}/logs/apple-gr_base.log");
        velocityProperties.setProperty("resource.loader", "classpath");
        velocityProperties.setProperty("classpath.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return velocityProperties;
    }

    @Bean
    public SAMLObjectFactory samlObjectFactory() {
        return new SAMLObjectFactory();
    }

    @Bean("samlAuthnRequestBuilder")
    public SAMLAuthnRequestBuilder samlAuthnRequestBuilder(
        @Autowired @Qualifier("samlObjectFactory") final SAMLObjectFactory samlObjectFactory) {
        final SAMLAuthnRequestBuilder samlAuthnRequestBuilder = new SAMLAuthnRequestBuilder();
        samlAuthnRequestBuilder.setAssertionConsumerPath("/SAML/POST");
        samlAuthnRequestBuilder.setSamlObjectFactory(samlObjectFactory);
        return samlAuthnRequestBuilder;
    }

    @Bean("userSessionService")
    public SAMLSessionService userSessionService() {
        return new SAMLSessionService();
    }

    @Bean("samlDateTimeSkewValidator")
    public SAMLDateTimeSkewValidator samlDateTimeSkewValidator() {
        return new SAMLDateTimeSkewValidator();
    }

    @Bean("keyStore")
    public SAMLKeyStoreFactoryBean keyStore(@Value("${saml.aplgr.keystore.password}") final char[] keyStorePassword,
        @Value("${saml.aplgr.keystore.resource}") final Resource keyStoreResource) {
        final SAMLKeyStoreFactoryBean samlKeyStoreFactoryBean = new SAMLKeyStoreFactoryBean();
        samlKeyStoreFactoryBean.setKeyStorePassword(keyStorePassword);
        samlKeyStoreFactoryBean.setKeyStoreResource(keyStoreResource);
        return samlKeyStoreFactoryBean;
    }


    @Bean("samlProtectionFilter")
    public AppleSAMLProtectionFilter samlProtectionFilter(
            @Autowired @Qualifier("protectionDeciderService")
            final ProtectionDeciderService protectionDeciderService,
            @Autowired @Qualifier("samlAuthnRequestBuilder") final SAMLAuthnRequestBuilder samlAuthnRequestBuilder,
            @Autowired @Qualifier("samlObjectFactory") final SAMLObjectFactory samlObjectFactory,
            @Autowired @Qualifier("userSessionService") final SAMLSessionService userSessionService,
            @Autowired @Qualifier("applicationProperties") final Properties applicationProperties,
            @Autowired @Qualifier("hostToEntityConfig") final Properties hostToEntityConfig)
    {
        final AppleSAMLProtectionFilter samlProtectionFilter = new AppleSAMLProtectionFilter();
        samlProtectionFilter.setUserSessionService(userSessionService);
        samlProtectionFilter.setSamlObjectFactory(samlObjectFactory);
        samlProtectionFilter.setSamlAuthnRequestBuilder(samlAuthnRequestBuilder);
        samlProtectionFilter.setProtectionDeciderService(protectionDeciderService);
        samlProtectionFilter.setProperties(applicationProperties);
        samlProtectionFilter.setHostToEntityConfig(hostToEntityConfig);
        return samlProtectionFilter;
    }


    @Bean("samlAssertionEncrypter")
    public SAMLAssertionEncrypter samlAssertionEncrypter(
        @Autowired @Qualifier("keyStore") final KeyStore keyStore,
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration) {

        final SAMLAssertionEncrypter samlAssertionEncrypter = new SAMLAssertionEncrypter();
        samlAssertionEncrypter.setKeyStore(keyStore);
        samlAssertionEncrypter.setSamlConfiguration(samlConfiguration);
        return samlAssertionEncrypter;
    }

    @Bean("samlObjectSigner")
    public SAMLObjectSigner samlObjectSigner(@Autowired @Qualifier("keyStore") final KeyStore keyStore,
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration) {

        final SAMLObjectSigner samlObjectSigner = new SAMLObjectSigner();
        samlObjectSigner.setKeyStore(keyStore);
        samlObjectSigner.setSamlConfiguration(samlConfiguration);
        return samlObjectSigner;
    }

    @Bean("samlResponseController")
    public SAMLResponseController samlResponseController(
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration,
        @Autowired @Qualifier("samlResponseProcessor") final SAMLResponseProcessor samlResponseProcessor,
        @Autowired @Qualifier("httpPostDecoder") final HTTPPostDecoder httpPostDecoder) {

        final SAMLResponseController samlResponseController = new SAMLResponseController();
        samlResponseController.setSamlConfiguration(samlConfiguration);
        samlResponseController.setSamlResponseProcessor(samlResponseProcessor);
        samlResponseController.setSuccessView("/login.do");
        samlResponseController.setMessageDecoder(httpPostDecoder);
        return samlResponseController;
    }

    @Bean("httpPostDecoder")
    public HTTPPostDecoder httpPostDecoder() {
        return new HTTPPostDecoder();
    }

    @Bean("samlResponseProcessor")
    public SAMLResponseProcessor samlResponseProcessor(@Value("${saml.responseSkew:60}") final int responseSkew,
        @Value("${saml.maxAssertionTime:60}") final int maxAssertionTime,
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration,
        @Autowired @Qualifier("userSessionService") final SAMLSessionService userSessionService,
        @Autowired @Qualifier("samlObjectSigner") final SAMLObjectSigner samlObjectSigner,
        @Autowired @Qualifier("samlAssertionEncrypter") final SAMLAssertionEncrypter samlAssertionEncrypter,
        @Autowired @Qualifier("samlDateTimeSkewValidator") final SAMLDateTimeSkewValidator samlDateTimeSkewValidator) {
        //ResponseSkew and MaxAssertionTime set to Default value of 60
        //We should be able to change the skew values only for troubleshooting during SAML integrations
        final SAMLResponseProcessor samlResponseProcessor = new SAMLResponseProcessor();

        samlResponseProcessor.setSamlConfiguration(samlConfiguration);
        samlResponseProcessor.setUserSessionService(userSessionService);
        samlResponseProcessor.setSamlObjectSigner(samlObjectSigner);
        samlResponseProcessor.setSamlAssertionEncrypter(samlAssertionEncrypter);
        samlResponseProcessor.setSamlDateTimeSkewValidator(samlDateTimeSkewValidator);

        samlResponseProcessor.setResponseSkew(responseSkew);
        samlResponseProcessor.setMaxAssertionAge(maxAssertionTime);
        return samlResponseProcessor;
    }

    @Bean("protectionDeciderService")
    public SAMLConfigurationPDS protectionDeciderService(
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration) {
        final SAMLConfigurationPDS samlConfigurationPDS = new SAMLConfigurationPDS();
        samlConfigurationPDS.setSamlConfiguration(samlConfiguration);
        return samlConfigurationPDS;
    }

    @Bean("buildEpsilonSAMLResponse")
    public BuildEpsilonSAMLResponse buildEpsilonSAMLResponse() {
        return new BuildEpsilonSAMLResponse();
    }

    @Bean("buildEpsilonSAMLResponseFromString")
    public BuildEpsilonSAMLResponseFromString buildEpsilonSAMLResponseFromString() {
        return new BuildEpsilonSAMLResponseFromString();
    }

    @Bean("samlConfiguration")
    public SAMLConfiguration samlConfiguration(
        @Autowired @Qualifier("hostToIdpConfig") final Properties hostToIdpConfig,
        @Autowired @Qualifier("hostToEntityConfig") final Properties hostToEntityConfig,
        @Autowired @Qualifier("samlAliasCredentials") final Properties samlAliasCredentials,
        @Autowired @Qualifier("applicationProperties") final Properties applicationProperties) {
        final SAMLConfiguration samlConfiguration = new SAMLConfiguration();
        samlConfiguration.setHostToIdpMapping(hostToIdpConfig);
        samlConfiguration.setHostToEntityMapping(hostToEntityConfig);
        samlConfiguration.setAliasCredentials(samlAliasCredentials);
        samlConfiguration.setEntityId("APLGR");

        final List<SecurityIntention> securityIntentionList = new ArrayList<>();
        final String securityIntentions =
            applicationProperties.getProperty(CommonConstants.SAML_SECURITY_INTENTIONS);
        // TO-DO: Keystone is working on solutioning to dynamically set Security Intentions based on Entity ID.
        // For now, we are going with environment specific Security Intentions.
        if (StringUtils.isNotBlank(securityIntentions)) {
            for (final String additionalSecurityIntention : securityIntentions.split(",")) {
                securityIntentionList.add(SecurityIntention.fromName(additionalSecurityIntention.trim()));
            }
        } else {
            securityIntentionList.add(SecurityIntention.NOOP);
        }
        samlConfiguration
            .setSecurityIntentions(securityIntentionList.toArray(SecurityIntention[]::new));
        return samlConfiguration;
    }

    @Bean("xmlDecrypter")
    public XMLDecrypter xmlDecrypter(@Autowired @Qualifier("keyStore") final KeyStore keyStore,
        @Autowired @Qualifier("samlConfiguration") final SAMLConfiguration samlConfiguration) {
        final XMLDecrypter xmlDecrypter = new XMLDecrypter();
        xmlDecrypter.setKeyStore(keyStore);
        xmlDecrypter.setSamlConfiguration(samlConfiguration);
        return xmlDecrypter;
    }
}
