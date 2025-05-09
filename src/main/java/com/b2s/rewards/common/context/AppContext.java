package com.b2s.rewards.common.context;

import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

public class AppContext {
    private static ApplicationContext ctx;

    /**
     * Injected from the class "ApplicationContextProvider" which is automatically
     * loaded during Spring-Initialization.
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    /**
     * Get access to the Spring ApplicationContext from everywhere in your Application.
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    /**
     * Get the Properties based on the specified Bean
     *
     * @return
     */
    public static Properties getAppProperties() {
        return ctx.getBean(CommonConstants.APPLICATION_PROPERTIES, Properties.class);
    }

    /**
     * Get a specific property
     *
     * @return
     */
    public static String getAppProperty(String key) {
        return getAppProperties().getProperty(key);
    }
}
