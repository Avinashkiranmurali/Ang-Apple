package com.b2s.rewards.security.controller;

import com.b2s.rewards.account.util.UserUtil;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CitiUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.security.saml.Throw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
/**
 * Created by ppalpandi on 11/27/2017.
 */
@Controller
@RequestMapping(value = "citi")
public class CitiLoginController {

    private static final Logger LOG = LoggerFactory.getLogger(CitiLoginController.class);

    public static final String RELAY_STATE ="/apple-gr/citi/SAML/POST";

    public static final String REDIRECTING_THE_USER_TO = "Redirecting the User to {}";
    public static final String REDIRECT = "redirect:";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private CitiUtil citiUtil;

    @PostConstruct
    public void setImageServerURL() {
        servletContext.setAttribute(CommonConstants.IMAGE_SERVER_KEY, applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY));
    }

    @RequestMapping(value = "/AppleStore", method = RequestMethod.GET)
    public String initSAMLFromEpsilon(@RequestParam(required = false) final String src,
        @RequestParam(required = false) final String CATEGORY,@RequestParam(required = false) final String itemId,@RequestParam(required = true) final String env,
                                            final HttpServletRequest request) {

        //Here is the example of the final URL that will be developed.
        //https://uat.cbgrus.uatglobalrewards.com/v2/loginSeamless.htm?partnerCode=FV_GRB2S&relayState=https%3A%2F%2Fcatalog.pat.cbgrus.uatglobalrewards.com%2Fapple-gr%252FSSOLogin.do%26sessionState%3DA%26reqOrigin%3Dinternal

        Throw.when("env", env).isNull();

        if (!citiUtil.isValidSAMLRequest(request,src,CATEGORY,itemId)){
            return ValidateLoginController.FAIL_VIEW;
        }
        //Check if the user session state is Logged in or OBO then do not request for SAML handoff
        //Also make sure the session did not timeout on Apple.
        String sessionState = (String) request.getSession().getAttribute(CommonConstants.CITI_SAML_SESSION_STATE);
        if (!UserUtil.isSessionTimeout(request) && (Arrays.asList("L", "O").contains(sessionState)) && (!citiUtil.isDeepLinkRequest(src,CATEGORY,itemId))){
            return ValidateLoginController.SUCCESS_VIEW;
        }
        //If we have anyon or no user session then we request IDP for a session
        final String relayState = citiUtil.buildSAMLRelayStateURL(request,src,CATEGORY,itemId);
        final String idpUrl = citiUtil.getIdentityProvider(request.getServerName(),env,false);
        final String redirectUrl = citiUtil.constructRedirectURL(CommonConstants.CITI_SAML_IDP_DEFAULT_SESSION_STATE,Optional.of(relayState.toString()),idpUrl);
        LOG.info(REDIRECTING_THE_USER_TO, redirectUrl);
        return REDIRECT + redirectUrl;
    }

    @RequestMapping(value = "/ViewOrder", method = RequestMethod.GET)
    public String handleOrderHistoryUrl(@RequestParam(required = true) final String orderId,
        final HttpServletRequest request) {
        Throw.when("orderId", orderId).isNull();

        final StringBuilder redirectUrl = new StringBuilder();
        final String appHostName = AppleUtil.getHostName(request);
        //When no user session available, redirect the Citi user to the Epsilon login seamless to get SAML data
        if (UserUtil.isSessionTimeout(request)){
            final StringBuilder relayState = new StringBuilder();
            relayState.append(appHostName).append(CommonConstants.ORDER_SUMMARY_ENDPOINT).append("orderId=").append(orderId);
            redirectUrl.append(citiUtil.buildEpsilonRedirectURL(CommonConstants.CITI_OBO_SESSION_STATE,relayState.toString(),appHostName,false));
        }
        //when user session available redirect the user to order history page
        else{
            redirectUrl.append(AppleUtil.getHostName(request)).append(CommonConstants.ORDER_SUMMARY_PATH).append(orderId);
        }
        LOG.info(REDIRECTING_THE_USER_TO, redirectUrl);
        return REDIRECT + redirectUrl.toString();
    }

    @RequestMapping(value = "/Terms", method = RequestMethod.GET)
    public String handleOrderHistoryUrl(final HttpServletRequest request) {
        final StringBuilder redirectUrl = new StringBuilder();
        final String appHostName = AppleUtil.getHostName(request);
        //When no user session available, redirect the Citi user to the Epsilon login seamless to get SAML data
        if (UserUtil.isSessionTimeout(request)){
            final StringBuilder relayState = new StringBuilder();
            relayState.append(appHostName).append(CommonConstants.CITI_APPLE_TERMS_ENDPOINT);
            redirectUrl.append(citiUtil.buildEpsilonRedirectURL(CommonConstants.CITI_OBO_ANONYMS_STATE,relayState.toString(),appHostName,false));
        }
        //when user session available redirect the user to terms page
        else{
            redirectUrl.append(AppleUtil.getHostName(request)).append(CommonConstants.CITI_TERMS_URL);
        }
        LOG.info(REDIRECTING_THE_USER_TO, redirectUrl);
        return REDIRECT + redirectUrl.toString();
    }

    @RequestMapping(value = "/Consent", method = RequestMethod.GET)
    public String handleConsent(final HttpServletRequest request){
        final String appHostName = AppleUtil.getHostName(request);
        final String redirectUrl = citiUtil.buildEpsilonRedirectURL(CommonConstants.CITI_SAML_IDP_LOGGED_IN_SESSION_STATE,AppleUtil.getHostName(request).concat(RELAY_STATE),appHostName,true);
        return REDIRECT +redirectUrl;
    }

}
