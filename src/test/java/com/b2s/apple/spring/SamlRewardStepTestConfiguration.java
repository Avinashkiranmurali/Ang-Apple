package com.b2s.apple.spring;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author rkumar 2020-02-04
 */
@Configuration
public class SamlRewardStepTestConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRewardStepTestConfiguration.class);

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

}
