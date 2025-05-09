package com.b2s.apple.config;

import com.b2s.rewards.apple.validator.AddressValidatorImpl;
import com.b2s.rewards.common.context.ApplicationContextProvider;
import com.b2s.rewards.common.context.DBVersionCheck;
import com.b2s.security.saml.controller.SAMLResponseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.util.Locale;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_ENCODING_UTF8;

/**
 * @author rkumar 2020-01-24
 */

@EnableWebMvc
@Configuration
@ImportResource({"classpath:kount-client-context.xml"})
@Import(value = {DaoConfig.class,
    ProductServiceConfig.class,
    RewardStepServletConfig.class,
    SamlRewardStepConfig.class,
    BasicAuthConfiguration.class,
    UtilConfig.class,
    VarConfig.class,
    ResourceConfig.class,
    CorePaymentServerConfig.class,
    CarouselAppConfig.class})
@ComponentScan(basePackages = {"com.b2s.rewards.merchandise.action",
    "com.b2s.rewards.security.util", "com.b2s.rewards.apple.util", "com.b2s.apple.util", "com.b2s.apple.mapper",
    "com.b2r.util.address.melissadata", "com.b2s.common.services", "com.b2s.apple.services",
    "com.b2s.rewards.apple.dao", "com.b2s.rewards.common.context", "com.b2s.shop.common.order",
    "com.b2s.security.oauth", "com.b2s.security.config", "com.b2s.shop.util", "com.b2s.spark",
    "com.b2s.rewards.apple.model", "com.b2s.idology", "com.b2s.security.idology", "com.b2r.service",
    "com.b2s.rewards.security.controller", "com.b2s.rewards.security.saml.citi", "com.b2s.rewards.apple.exceptionhandler",
    "com.b2r.paymentserver.api"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SAMLResponseController.class),
    })
public class ApplicationConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        final Resource applicationResource = new ClassPathResource("application.properties");
        final Resource environmentResource = new ClassPathResource("environment.properties");
        final Resource imageProxyResource = new ClassPathResource("image-proxy-client.properties");

        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
            new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(applicationResource, environmentResource, imageProxyResource);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertySourcesPlaceholderConfigurer.setFileEncoding(DEFAULT_ENCODING_UTF8);
        propertySourcesPlaceholderConfigurer.setOrder(10000000);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public PropertiesFactoryBean applicationProperties() {
        final Resource applicationResource = new ClassPathResource("application.properties");
        final Resource environmentResource = new ClassPathResource("environment.properties");

        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocations(applicationResource, environmentResource);
        return propertiesFactoryBean;
    }

    @Bean
    public AddressValidatorImpl addressValidator() {
        return new AddressValidatorImpl();
    }

    @Bean
    public ApplicationContextProvider contextApplicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean(initMethod = "checkDbVersion")
    public DBVersionCheck dbVersionCheck(@Autowired @Qualifier("dataSource") final DataSource dataSource) {
        final DBVersionCheck dbVersionCheck = new DBVersionCheck();
        dbVersionCheck.setDataSource(dataSource);
        return dbVersionCheck;
    }

    //Locale Resolver
    @Bean
    public SessionLocaleResolver localeResolver() {
        final SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return sessionLocaleResolver;
    }

    @Bean
    public BCryptPasswordEncoder bcryptEncoder(@Autowired @Qualifier("secureRandom") final SecureRandom secureRandom) {
        return new BCryptPasswordEncoder(13, secureRandom);
    }

    @Bean("secureRandom")
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
