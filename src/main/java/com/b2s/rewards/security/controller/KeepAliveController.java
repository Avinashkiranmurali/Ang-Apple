package com.b2s.rewards.security.controller;

import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by rpillai on 2/15/2016.
 */
@Controller
public class KeepAliveController {

    protected static final String CONTENT_TYPE = "application/javascript";

    private ObjectMapper objectMapper;
    private static final Logger LOG = LoggerFactory.getLogger(KeepAliveController.class);


    @Autowired
    private Properties applicationProperties;

    @PostConstruct
    public void afterPropertiesSet() {
        objectMapper= new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @RequestMapping(
            value = "/"+ExternalUrlConstants.KEEP_ALIVE_URL_PARTNER_REQ_MAPPING,
            method = {RequestMethod.GET},
            produces = {CONTENT_TYPE})
    public void keepAlivePartner(@RequestParam(value = "partnerId", required=true) final String partnerId,
                          @RequestParam(value = "callback", required=false) final String callback,
                          final HttpServletRequest request,
                          final HttpServletResponse response, final HttpSession httpSession) throws
            ServletException, IOException {
        LOG.info("Keep alive request from partner {}", partnerId);
        keepAlive(false, request, response, httpSession);
    }


    /**
     * Handle options.
     *
     * @param response the response
     */
    @RequestMapping(value = {"/keepAlive"}, method = RequestMethod.OPTIONS)
    public void handleOptions(final HttpServletResponse response, final HttpServletRequest servletRequest) {
        XSSRequestWrapper request = new XSSRequestWrapper(servletRequest);
        AppleUtil.setAllowCORSHeaders(request,response);
    }
    @RequestMapping(value = {"/keepAlive"}, method = RequestMethod.GET)
    public void handleOptionsGet(final HttpServletResponse response,final HttpServletRequest servletRequest) {
        XSSRequestWrapper request = new XSSRequestWrapper(servletRequest);
        AppleUtil.setAllowCORSHeaders(request,response);
    }

    @RequestMapping(
            value = "/"+ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE_REQ_MAPPING,
            method = {RequestMethod.GET},
            produces = {CONTENT_TYPE})
    public ResponseEntity<Void> keepAlive(@RequestParam(value = "initial", required=false, defaultValue="false") final boolean initial,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse response, final HttpSession httpSession) throws
            ServletException, IOException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final String callback = request.getParameter("callback");
        if(!"JSON_CALLBACK".equals(callback)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user = null;
        if(request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT) != null) {
            user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        }
        if( httpSession.isNew()) {
            throw new InvalidSessionException();
        }
        LOG.info("Keep Alive called {} {}", callback, request.getRemoteHost());
        Assert.hasText(callback);

        final String jsonpCallbackScript= "%s(%s);";

        final String callbackParameters=
                objectMapper
                        .writer()
                        .writeValueAsString(populateKeepAlives(user, request, initial));

        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        try(final PrintWriter printWriter = response.getWriter()) {
            printWriter.println(String.format(jsonpCallbackScript, callback, callbackParameters));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private KeepAlives populateKeepAlives(final User user, final HttpServletRequest request,
                                          final boolean initial) {

        final String keepAliveUrl = getUrl(user, ExternalUrlConstants.KEEP_ALIVE_URL, request);

        final KeepAlives keepAlives = populateKeepAlivesInitial(user, request, initial);

        if (CommonConstants.isScotiaUser(user)) {

            keepAlives.setKeepAliveJSONP(Boolean.valueOf(CommonConstants.getExternalUrl(request, ExternalUrlConstants.KEEP_ALIVE_JSONP)));

            if (StringUtils.isNotBlank(keepAliveUrl)) {
                keepAlives.setKeepAliveUrl(keepAliveUrl + "?=" + System.currentTimeMillis() + "&callback=JSON_CALLBACK");
            }
        } else {
            if (StringUtils.isNotBlank(keepAliveUrl)) {
                keepAlives.setKeepAliveUrl(keepAliveUrl);
            }
        }
        return keepAlives;
    }

    private KeepAlives populateKeepAlivesInitial(final User user, final HttpServletRequest request,
                                                 final boolean initial) {
        KeepAlives keepAlives = new KeepAlives();
        final String navigateBackUrl = getUrl(user, ExternalUrlConstants.NAVIGATE_BACK_URL, request);
        final String signOutUrl = getUrl(user, ExternalUrlConstants.SIGN_OUT_URL, request);
        final String timeOutUrl = getUrl(user, ExternalUrlConstants.TIME_OUT_URL, request);
        final String homeLinkUrl = getUrl(user, ExternalUrlConstants.HOME_LINK_URL, request);
        if (initial) {
            if (StringUtils.isNotBlank(navigateBackUrl)) {
                keepAlives.setNavigateBackUrl(navigateBackUrl);
            }
            if (StringUtils.isNotBlank(homeLinkUrl)) {
                keepAlives.setHomeLinkUrl(homeLinkUrl);
            }
            if (StringUtils.isNotBlank(signOutUrl)) {
                keepAlives.setSignOutUrl(signOutUrl);
            }
            if (StringUtils.isNotBlank(timeOutUrl)) {
                keepAlives.setTimeOutUrl(timeOutUrl);
            }
        }
        return keepAlives;
    }

    private String getUrl(final User user, final String key, final HttpServletRequest request) {
        String url = "";
        if (CommonConstants.getExternalUrl(request,key) != null) {
            return CommonConstants.getExternalUrl(request,key);
        }
        if(user != null && StringUtils.isNotBlank(user.getVarId()) && StringUtils.isNotBlank(user.getProgramId())) {
            url = CommonConstants.getApplicationProperty(key, user.getVarId(), user.getProgramId(), user.getLocale(), applicationProperties);
        }
        return url;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgumentException() {
        // Do Nothing
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    private static class InvalidSessionException extends RuntimeException {
        private static final long serialVersionUID = 4085490563562902633L;
    }

    public static class KeepAlives {
        private String navigateBackUrl;
        private String homeLinkUrl;
        private String signOutUrl;
        private String timeOutUrl;
        private String keepAliveUrl;
        private String keepAliveUrlCORE;
        private boolean keepAliveJSONP=false;

        public String getNavigateBackUrl() {
            return navigateBackUrl;
        }

        public void setNavigateBackUrl(final String navigateBackUrl) {
            this.navigateBackUrl = navigateBackUrl;
        }

        public String getHomeLinkUrl() {
            return homeLinkUrl;
        }

        public void setHomeLinkUrl(final String homeLinkUrl) {
            this.homeLinkUrl = homeLinkUrl;
        }

        public String getSignOutUrl() {
            return signOutUrl;
        }

        public void setSignOutUrl(final String signOutUrl) {
            this.signOutUrl = signOutUrl;
        }

        public String getTimeOutUrl() {
            return timeOutUrl;
        }

        public void setTimeOutUrl(final String timeOutUrl) {
            this.timeOutUrl = timeOutUrl;
        }

        public String getKeepAliveUrl() {
            return keepAliveUrl;
        }

        public void setKeepAliveUrl(final String keepAliveUrl) {
            this.keepAliveUrl = keepAliveUrl;
        }

        public String getKeepAliveUrlCORE() {
            return keepAliveUrlCORE;
        }

        public void setKeepAliveUrlCORE(String keepAliveUrlCORE) {
            this.keepAliveUrlCORE = keepAliveUrlCORE;
        }

        public boolean isKeepAliveJSONP() {
            return keepAliveJSONP;
        }

        public void setKeepAliveJSONP(final boolean keepAliveJSONP) {
            this.keepAliveJSONP = keepAliveJSONP;
        }
    }
}
