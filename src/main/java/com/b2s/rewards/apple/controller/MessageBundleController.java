package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.common.constant.Constant;
import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import static com.b2s.rewards.common.util.CommonConstants.MAINTENANCE_MESSAGE_SESSION_OBJECT;

/**
 * Created by meddy on 6/25/2015.
 */
@RestController
@SessionAttributes(CommonConstants.USER_SESSION_OBJECT)
public class MessageBundleController {

    private static final String MESSAGE_SOURCE = "messageSource";
    private static final String MAINTENANCE_MESSAGE = "maintenanceMessage";
    private static final Logger LOG = LoggerFactory.getLogger(MessageBundleController.class);

    /**
     * ReadAll
     */
    @RequestMapping(value = {"/messages"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> list(final HttpServletRequest request) {
        String locale = null;
        String varId = null;
        String programId = null;
        User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        if (user != null) {
            locale = XSSRequestWrapper.cleanXSS(user.getLocale().toString());
            varId = XSSRequestWrapper.cleanXSS(user.getVarId());
            programId = XSSRequestWrapper.cleanXSS(user.getProgramId());
        }

        if(programId == null || varId ==null || locale == null) {
            return ResponseEntity.badRequest().body("Please provide locale, varid and program id");
        }

        B2RReloadableResourceBundleMessageSource messageSource ;
        try {
            messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean(
                    MESSAGE_SOURCE + varId);
        }catch (BeansException e){
            LOG.warn("No message source definition found for the given varId : {}." +
                    " Fetching default messageSource", varId);
            messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean(MESSAGE_SOURCE);
        }
        messageSource.setVarId(varId);
        messageSource.setProgramId(programId);

        Properties properties = Optional.ofNullable(messageSource.getAllMessages(LocaleUtils.toLocale(locale), null))
                .orElseGet(Properties::new);
        return ResponseEntity.ok(properties);
    }

    /**
     * ReadAll
     */
    @RequestMapping(value = {"/publicMessages"}, method = RequestMethod.GET)
    @ResponseBody
    public Properties getPublicMessages(final HttpServletRequest request,
        @RequestParam(required = false, value = "locale") final String localeParam,
        @RequestParam(required = false, value = "code_type") final String codeTypeParam) {

        String locale = XSSRequestWrapper.cleanXSS(localeParam);
        Locale localeObj = StringUtils.isNotBlank(locale) ? LocaleUtils.toLocale(locale) : Locale.US;

        String codeType =
            StringUtils.isNotBlank(codeTypeParam) ? AppleUtil.sanitizeInputString(codeTypeParam) : Constant.CODE_TYPE_LOGIN;

        if (Constant.CODE_TYPE_MAINTENANCE.equalsIgnoreCase(codeType)) {
            Properties properties = new Properties();
            final String maintenanceMessage =
                HtmlUtils.htmlEscape((String) request.getSession().getAttribute(MAINTENANCE_MESSAGE_SESSION_OBJECT));
            if (StringUtils.isNotBlank(maintenanceMessage)) {
                properties.put(MAINTENANCE_MESSAGE, maintenanceMessage);
            }
            return properties;
        }

        B2RReloadableResourceBundleMessageSource messageSource ;
        try {
           messageSource = (B2RReloadableResourceBundleMessageSource)
                    AppContext.getApplicationContext().getBean("publicMessageSource");
        }catch (BeansException e){
            messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean(
                MESSAGE_SOURCE);
        }

        return Optional.ofNullable(messageSource.getAllMessages(localeObj, codeType)).orElseGet(Properties::new);
    }
}
