package com.b2s.rewards.apple.util;

import com.b2s.rewards.account.util.UserUtil;
import static com.b2s.rewards.common.util.CommonConstants.*;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.var.VAROrderManagerCiti;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ppalpandi on 10/27/2017.
 */
@Component
public class CitiUtil {

    public static final String RETURNING = "Returning: {}";
    private static Logger log = LoggerFactory.getLogger(AppleUtil.class);

    @Autowired
    private Properties applicationProperties;

    private static final String URL_FORMAT = "https://%1$s/%2$s";

    private static final String CONSENT = "consent.htm";

    private static final String LOGIN_SEAMLESS = "loginSeamless.htm";


    private final Map<String, String> epsilonHostNameBySourceCode = new HashMap<>();

    private final List<String> hostNameKeys = Arrays.asList(WEBAPP_HOSTNAME_GRCS,WEBAPP_HOSTNAME_GRBQ,WEBAPP_HOSTNAME_GRDI,WEBAPP_HOSTNAME_GRSC);


    @PostConstruct
    private void setupHostMaps() {
        epsilonHostNameBySourceCode.put(WEBAPP_HOSTNAME_GRCS, applicationProperties.getProperty(EPSILON_HOSTNAME_GRCS));
        epsilonHostNameBySourceCode.put(WEBAPP_HOSTNAME_GRBQ, applicationProperties.getProperty(EPSILON_HOSTNAME_GRBQ));
        epsilonHostNameBySourceCode.put(WEBAPP_HOSTNAME_GRDI, applicationProperties.getProperty(EPSILON_HOSTNAME_GRDI));
        epsilonHostNameBySourceCode.put(WEBAPP_HOSTNAME_GRSC, applicationProperties.getProperty(EPSILON_HOSTNAME_GRSC));
    }

    public String getEpsilonHostName(final String webAppHostName) {

        return hostNameKeys.stream().filter(
                key->webAppHostName.contains(key)).findFirst().map(
                key->epsilonHostNameBySourceCode.get(key)).
                orElse(applicationProperties.getProperty(EPSILON_HOSTNAME));
    }

    private String getIdentityProvider(final String webAppHostName, final boolean forConsent) {
        final StringBuilder identityProvider = new StringBuilder();
        identityProvider.append(getEpsilonHostName(webAppHostName)).append('/').append(forConsent ? CONSENT :LOGIN_SEAMLESS);
        log.debug(RETURNING, identityProvider);
        return identityProvider.toString();
    }

    public String buildEpsilonRedirectURL(final String sessionState,final String relayState,final String webAppHostName,final boolean forConsent) {
        return constructRedirectURL(sessionState, Optional.of(relayState), getIdentityProvider(webAppHostName, forConsent));
    }

    public String constructRedirectURL(final String sessionState,
        final Optional<String> relayState,
        final String identityProviderURL) {
        final String relayStateLog = relayState.orElse(null);
        log.debug("BuildingRedirectURL for SessionState: {}, RelayState: {}, IdentityProviderURL: {}",
            sessionState, relayStateLog, identityProviderURL);
        try {
            final StringBuilder redirectURL = new StringBuilder(256);

            redirectURL.append(identityProviderURL).append('?');

            redirectURL.append("partnerCode=").append(applicationProperties.getProperty(APPLE_PARTNER_CODE));
            redirectURL.append("&sessionState=").append(sessionState);
            if (relayState.isPresent()) {
                redirectURL.append("&relayState=").append(URLEncoder.encode(relayState.get(), "UTF-8"));
            }

            log.debug(RETURNING, redirectURL);
            return redirectURL.toString();
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Should never happen, but if it does, propagate as a RuntimeError", e);
        }
    }

    /*This will be removed once one to one environment mapping is available.
    * Currently Citi Apple PAT is supporting Epsilon's multiple lower environments*/
    public String getIdentityProvider(final String webAppHostName, final String environment,final boolean forConsent) {
        log.debug("Looking up IdentityProvider by WebAppHostName: {}", webAppHostName);
        final String hostNameKey = "Epsilon."+Stream.of("cardservices","boq","diners","suncorp").filter((key)->webAppHostName.contains(key)).findFirst().orElse("common")+"."+environment;
        final String identityProvider = String.format(URL_FORMAT,applicationProperties.getProperty(hostNameKey) , forConsent?CONSENT:LOGIN_SEAMLESS);
        log.debug(RETURNING, identityProvider);
        return identityProvider;
    }

    public boolean isValidSAMLRequest(final HttpServletRequest servletRequest, final String source,final String category, final String itemId){
        if(null!=source && (StringUtils.isNotEmpty(category) || StringUtils.isNotEmpty(itemId))){
            if(source.length()==0){
                return false;
            }
            if(!UserUtil.isSessionTimeout(servletRequest)){
                final User sessionUser = UserUtil.getUserFromSession(servletRequest.getSession());
                final VAROrderManagerCiti.Language language = VAROrderManagerCiti.Language.fromTwoLetterCode(sessionUser.getLocale().getLanguage());
                final StringBuilder builder = new StringBuilder(CITIGR_CODE).append(sessionUser.getCountry()).append(language.getThreeLetterCode());
                return source.equalsIgnoreCase(builder.toString());
            }
            return true;
        }
        return true;
    }

    public boolean isDeepLinkRequest(final String source,final String category, final String itemId){
        if((StringUtils.isNotEmpty(category) || StringUtils.isNotEmpty(itemId)) && StringUtils.isNotEmpty(source)){
            return true;
        }
        return false;
    }

    public String buildSAMLRelayStateURL(final HttpServletRequest servletRequest,final String source,final String category, final String itemId){
        final StringBuilder relayState = new StringBuilder();
        //Apple landing page as relay state URL
        relayState.append(AppleUtil.getHostName(servletRequest)).append(APPLE_LANDING_ENDPOINT);

        //Apending Deep Link parameters to relay state
        if(StringUtils.isNotBlank(source)){
            if (StringUtils.isNotBlank(itemId)){
                relayState.append(QUESTION).append(ITEM_ID).append(EQUAL).append(itemId);
                relayState.append(AND).append(CITIGR_SRC).append(EQUAL).append(source);
            }
            else if (StringUtils.isNotBlank(category)){
                relayState.append(QUESTION).append(CATEGORY).append(EQUAL).append(category);
                relayState.append(AND).append(CITIGR_SRC).append(EQUAL).append(source);
            }
        }
        return relayState.toString();
    }

}
