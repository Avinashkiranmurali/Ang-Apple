package com.b2s.rewards.apple.util;

import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Created by rpillai on 10/10/2016.
 */
@Component
public class ContextUtil {

    @Autowired
    private ApplicationContext ctx;
    private static final Logger LOG = LoggerFactory.getLogger(ContextUtil.class);

    public MessageSource getMessageSource(String varId) {
        B2RReloadableResourceBundleMessageSource messageSource;
        try    {
            messageSource = (B2RReloadableResourceBundleMessageSource) ctx.getBean("messageSource"+varId);
        }catch (BeansException e){
            LOG.warn("No message source definition found for the given varId : {}." +
                    " Fetching default messageSource", varId);
            messageSource = (B2RReloadableResourceBundleMessageSource) ctx.getBean("messageSource");
        }
        return messageSource;
    }
}
