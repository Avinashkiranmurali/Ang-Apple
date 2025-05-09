package com.b2s.apple.config;

import com.b2s.apple.services.SAMLSessionService;
import com.b2s.rewards.apple.interceptor.SessionLoginInterceptor;
import com.b2s.rewards.apple.util.CustomResourceBundleMessageSource;
import com.b2s.security.saml.interceptor.SAMLProtectionWithSignInterceptor;
import com.b2s.security.saml.service.SAMLAuthnRequestBuilder;
import com.b2s.security.saml.service.impl.RefererBasedPDS;
import com.b2s.security.saml.util.SAMLObjectFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.security.KeyStore;
import java.util.Locale;

@ComponentScan(basePackages = {"com.b2s.rewards.security.controller",
    "com.b2s.rewards.util","com.b2s.rewards.apple.controller","com.b2s.idology.controller","com.b2s.apple.config", "com.b2r.paymentserver.api"})
@Configuration
@EnableWebMvc
public class RewardStepServletConfig implements WebMvcConfigurer {

    final VelocityEngine samlVelocityEngine;
    final KeyStore keyStore;
    final SAMLObjectFactory samlObjectFactory;
    final RefererBasedPDS protectionDeciderServiceReferBasedPDS;
    final SAMLAuthnRequestBuilder samlAuthnRequestBuilder;
    final SAMLSessionService userSessionService;

    @Autowired
    public RewardStepServletConfig(
        final VelocityEngine samlVelocityEngine,
        final KeyStore keyStore,
        final SAMLObjectFactory samlObjectFactory,
        final RefererBasedPDS protectionDeciderServiceReferBasedPDS,
        final SAMLAuthnRequestBuilder samlAuthnRequestBuilder,
        final SAMLSessionService userSessionService) {
        this.samlVelocityEngine = samlVelocityEngine;
        this.keyStore = keyStore;
        this.samlObjectFactory = samlObjectFactory;
        this.protectionDeciderServiceReferBasedPDS = protectionDeciderServiceReferBasedPDS;
        this.samlAuthnRequestBuilder = samlAuthnRequestBuilder;
        this.userSessionService = userSessionService;
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        final Resource environmentResource = new ClassPathResource("environment.properties");
        final Resource applicationResource = new ClassPathResource("application.properties");

        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
            new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(applicationResource, environmentResource);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertySourcesPlaceholderConfigurer.setFileEncoding("UTF-8");
        propertySourcesPlaceholderConfigurer.setOrder(10000000);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean("viewResolver")   //UrlBasedViewResolver
    public UrlBasedViewResolver viewResolver() {
        final UrlBasedViewResolver urlBasedViewResolver = new UrlBasedViewResolver();
        urlBasedViewResolver.setViewClass(InternalResourceView.class);
        urlBasedViewResolver.setOrder(0);
        return urlBasedViewResolver;
    }
    @Bean   //ResourceBundleViewResolver
    public ResourceBundleViewResolver ResourceBundleViewResolverBean() {
        final ResourceBundleViewResolver resourceBundleViewResolver = new ResourceBundleViewResolver();
        resourceBundleViewResolver.setOrder(0);
        return resourceBundleViewResolver;
    }

    @Bean("localeResolver")   //localeResolver
    public SessionLocaleResolver localeResolver() {
        final SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(new Locale("en"));
        return sessionLocaleResolver;
    }

    @Bean("multipartResolver")   //multipartResolver
    public CommonsMultipartResolver multipartResolver() {
        final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }


    @Bean
    public SAMLProtectionWithSignInterceptor samlProtectionWithSignInterceptor() {
        return new SAMLProtectionWithSignInterceptor(protectionDeciderServiceReferBasedPDS,
            samlAuthnRequestBuilder, samlObjectFactory, userSessionService, keyStore, samlVelocityEngine);
    }

    //Configuring interceptors based on URI
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new SessionLoginInterceptor());
        registry.addInterceptor(localeChangeInterceptorBean());

        registry.addInterceptor(samlProtectionWithSignInterceptor())
            .addPathPatterns("/ssoLoginAction.*");
    }


    @Bean   //LocaleChangeInterceptor
    public LocaleChangeInterceptor localeChangeInterceptorBean() {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Bean("customMessageSource")
    public CustomResourceBundleMessageSource messageSource(){
        final CustomResourceBundleMessageSource customResourceBundleMessageSource =
            new CustomResourceBundleMessageSource();
        customResourceBundleMessageSource.setBasename("classpath:/messages");
        return customResourceBundleMessageSource;
    }

    @Bean
    public ViewResolver internalViewResolver() {
        final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setExposeContextBeansAsAttributes(true);
        viewResolver.setPrefix("/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setOrder(2);
        return viewResolver;
    }
}
