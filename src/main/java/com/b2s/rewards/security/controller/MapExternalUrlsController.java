package com.b2s.rewards.security.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.util.OAuthRequestParam;
import com.b2s.apple.services.ProgramService;
import com.b2s.security.oauth.OAuthAttributes;
import com.b2s.security.oauth.service.OAuthTokenService;
import com.b2s.security.oauth.Token;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.b2s.rewards.security.util.ExternalUrlConstants.*;

@RestController
public class MapExternalUrlsController {

    private static final Logger LOG = LoggerFactory.getLogger(MapExternalUrlsController.class);
    public static final String APPLE_GR_PAGES = "/apple-gr/pages/";
    public static final String UPDATED_BALANCE = "updatedPointsBalance";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    protected ProgramService programService;

    @Autowired
    private OAuthTokenService oAuthTokenService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    /**
     * ReadAll
     */
    @RequestMapping(value = {"/validSession"}, method = RequestMethod.GET)
    public Map isSessionValid(@RequestParam(required = false, value = "initial") boolean initial,
        HttpServletRequest servletRequest) {
        XSSRequestWrapper request = new XSSRequestWrapper(servletRequest);
        final User user = appSessionInfo.currentUser();
        HashMap<String, Serializable> urls = (HashMap<String, Serializable>) request.getSession().getAttribute(EXTERNAL_URLS);
        if(MapUtils.isEmpty(urls)) {
            urls = new HashMap<>();

            final String oauthSessionState = getUpdatedOAuthSessionState(request);
            if(StringUtils.isNotBlank(oauthSessionState)){
                urls.put(CommonConstants.OAUTH_TOKEN_SESSION_STATE, XSSRequestWrapper.cleanXSS(oauthSessionState));
            }
            request.getSession().setAttribute(EXTERNAL_URLS, urls);
        } else {
            //Replace the keepalive URL only if the BASE_KEEP_ALIVE is set.
            //This ensures other VARS will not be impacted with this change.
            final String baseKeepAliveUrl = (String) urls.get(BASE_KEEP_ALIVE_URL);
            if (StringUtils.isNotBlank(baseKeepAliveUrl)) {
                urls.put(KEEP_ALIVE_URL, baseKeepAliveUrl + "?" + System.currentTimeMillis());
            }
        }
        setDefaultExternalUrls(request, urls, user.getLoginType());

        if (user.isUpdateBalance()) {
            user.setUpdateBalance(false);
            urls.put(UPDATED_BALANCE, String.valueOf(user.getBalance()));
        } else {
            urls.remove(UPDATED_BALANCE);
        }

        final Map<String, Serializable> returnUrls = new HashMap<>(urls);
        if(!initial){
            returnUrls.remove(KEYSTONE_URLS);
        }
        return returnUrls;
    }

    private void setDefaultExternalUrls(final HttpServletRequest servletRequest, HashMap<String, Serializable> urls,
        final String loginType){
        final String returnHost = AppleUtil.getHostName(servletRequest);

        // Redirect url set for five box and OTP login and not for client
        String redirectPage = CommonConstants.LOGIN_5_BOX_PAGE;
        if(CommonConstants.OTP.equalsIgnoreCase(loginType)){
            redirectPage = CommonConstants.LOGIN_OTP_PAGE;
        }

        if (StringUtils.isNotBlank(loginType) && CommonConstants.LoginType.FIVEBOX.getValue().equalsIgnoreCase(loginType)){
            final String navigateBackUrl = (String) urls.get(NAVIGATE_BACK_URL);
            final String timeOutUrl = (String) urls.get(TIME_OUT_URL);
            final String signOutUrl = (String) urls.get(SIGN_OUT_URL);
            if (StringUtils.isBlank(navigateBackUrl)){
                urls.put(NAVIGATE_BACK_URL, returnHost + APPLE_GR_PAGES + redirectPage +
                    "?returnTest=navigateBack");
            }
            if (StringUtils.isBlank(timeOutUrl)){
                urls.put(TIME_OUT_URL, returnHost + APPLE_GR_PAGES + redirectPage+"?returnTest=timeOutBack");
            }
            if (StringUtils.isBlank(signOutUrl)){
                urls.put(SIGN_OUT_URL, returnHost + APPLE_GR_PAGES + redirectPage+"?returnTest=signOutBack");
            }
        }
    }

    @RequestMapping(value = {"/timedOut"}, method = RequestMethod.GET)
    public ResponseEntity isTimedOut(HttpServletRequest servletRequest) {
        final User user = appSessionInfo.currentUser();
        String sessionTimeoutInMinutesStr = applicationProperties.getProperty(user.getVarId().toLowerCase() + "." + CommonConstants.WIDGET_TIMEOUT_MINUTES);
        if(StringUtils.isNotBlank(sessionTimeoutInMinutesStr)) {
            int sessionTimeoutInMinutes = Integer.valueOf(sessionTimeoutInMinutesStr);
            HashMap<String, Serializable> response = new HashMap<>();
            if(MapUtils.isNotEmpty(user.getAdditionalInfo()) && user.getAdditionalInfo().containsKey(CommonConstants.LOGIN_TIME)) {
                String loginDateStr = null;
                try {
                    loginDateStr = user.getAdditionalInfo().get(CommonConstants.LOGIN_TIME);
                    Date loginTime = new SimpleDateFormat(CommonConstants.DATE_TIME_FORMAT).parse(loginDateStr);
                    Date expiredTime = DateUtils.addMinutes(loginTime, sessionTimeoutInMinutes);
                    Date currentTime = new Date();
                    if(expiredTime.before(currentTime)) {
                        response.put("timedOut", true);
                        response.put("timedOutUrl", MessageFormat.format((String) applicationProperties.get(CommonConstants.UA_MPCONNECT_URL_KEY), applicationProperties.get(CommonConstants.UA_MPCONNECT_CID_KEY), user.getAdditionalInfo().get(OAuthRequestParam.ONLINE_AUTH_CODE.getValue())));
                    } else {
                        response.put("timedOut", false);
                    }
                } catch(ParseException pe) {
                    LOG.error("Error while parsing date string: {} ", loginDateStr, pe);
                }
            }
            return new ResponseEntity(response, HttpStatus.OK);
        }
        return new ResponseEntity("Not supported", HttpStatus.OK);
    }

    @RequestMapping(value = {"/postExternalUrls"},method=RequestMethod.POST)
    public Map postExternalUrls(final HttpServletRequest request,
                                final HttpSession session,
                                @RequestBody HashMap<String, Serializable> urls) {
        if(urls!=null && urls.containsKey(KEEP_ALIVE_URL)) {
                urls.put(KEEP_ALIVE_URL_VAR, urls.get(KEEP_ALIVE_URL));
        }
        HashMap<String, Serializable> existingUrls = (HashMap<String, Serializable>) session.getAttribute(EXTERNAL_URLS);
        if(existingUrls ==null){
            existingUrls = new HashMap<>();
        }
        if (urls != null) {
            existingUrls.putAll(urls);
            existingUrls = addOrUpdateUrls(request, existingUrls);
        }
        return existingUrls;
    }


    private HashMap<String, Serializable> addOrUpdateUrls(final HttpServletRequest request, final HashMap<String, Serializable> existingUrls) {
        //get existing external_urls
        final String hostName = AppleUtil.getHostName(request);
        final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        final User user = appSessionInfo.currentUser();
        HashMap<String, Serializable> externalUrls = programService.addOrUpdateExternalUrls(program, null, hostName, user.getLocale().toString(), existingUrls);
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
        return existingUrls;
    }

    private String getUpdatedOAuthSessionState(final HttpServletRequest request){
        OAuthAttributes oAuthAttributes =
            (OAuthAttributes)request.getSession().getAttribute(CommonConstants.OAUTH_ATTRIBUTES);
        if(Objects.nonNull(oAuthAttributes)){
            Token currentToken = oAuthAttributes.getToken();
            if(Objects.nonNull(currentToken)){
                if(oAuthTokenService.isTokenValid(currentToken)){
                    return currentToken.getSessionState();
                }else{
                    Token newToken = oAuthTokenService.refreshToken(oAuthAttributes);
                    return newToken.getSessionState();
                }
            }
        }
        return null;
    }

}
