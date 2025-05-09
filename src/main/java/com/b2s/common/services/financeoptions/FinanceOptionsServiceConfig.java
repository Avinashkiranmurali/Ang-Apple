package com.b2s.common.services.financeoptions;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Service Locator configuration
 */
@Configuration
public class FinanceOptionsServiceConfig {

    @Bean("financeOptionsServiceFactory")
    public FactoryBean serviceLocatorFactoryBean() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(FinanceOptionsServiceFactory.class);
        return factoryBean;

    }
}
