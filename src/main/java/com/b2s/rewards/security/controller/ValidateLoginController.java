package com.b2s.rewards.security.controller;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.services.*;
import com.b2s.common.services.discountservice.CouponCodeValidator;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.account.util.UserUtil;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CitiNavigationTarget;
import com.b2s.rewards.apple.util.CitiUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.SessionUtil;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.security.oauth.EnumOAuthInputParameter;
import com.b2s.security.oauth.OAuthConfig;
import com.b2s.security.oauth.OAuthCredentials;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.common.domain.response.ProductSearchDocumentGroup;
import com.b2s.service.product.common.domain.response.ProductSearchResponse;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.util.OAuthRequestParam;
import com.b2s.shop.common.order.var.VAROrderManagerIF;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.apple.util.AppleUtil.getClientIpAddress;
import static com.b2s.rewards.common.util.CommonConstants.FIVE9_TITLE_DEFAULT;
import static com.b2s.rewards.common.util.CommonConstants.FIVE9_TITLE_KEY;
import static com.b2s.rewards.common.util.CommonConstants.XSRF_TOKEN_REQUEST_HEADER_KEY;
import static com.b2s.rewards.common.util.CommonConstants.HYPHEN;
import static com.b2s.rewards.common.util.CommonConstants.DOT;
import static com.b2s.rewards.common.util.CommonConstants.DOT_ENDPOINT;
import static com.b2s.rewards.common.util.CommonConstants.ENDPOINT;
import static com.b2s.rewards.common.util.CommonConstants.MAINTENANCE_MESSAGE_SESSION_OBJECT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class ValidateLoginController {

    public static final String SUCCESS_VIEW = "redirect:/ui/store";
    public static final String WIDGET_VIEW = "redirect:/merchandise/widget.jsp";
    public static final String FAIL_VIEW = "redirect:/ui/login-error";
    public static final String DISCOUNT_ERROR_VIEW = "redirect:/ui/error";
    public static final String LOGIN_VIEW = "redirect:/pages/login.jsp";
    public static final String BLACKLIST_VIEW = "redirect:/pages/loginErrorAD.jsp";
    private static final String LOGIN_VIEW_WITH_RETURN = "redirect:/pages/login.jsp?returnTest=signOutBack";
    public static final String RELAY_STATE ="/apple-gr/SAML/POST";
    public static final String SAML_RELAY_STATE ="RelayState";
    // Same URL till production... If it varies bases on environment then we will move to environment property file
    public static final String MAINTENANCE = "redirect:https://als-static.rewardstep.com/maintenance/";
    public static final String BRIDGE2="bridge2";
    public static final String LOCALHOST="localhost";
    public static final String BASE_DEEPLINK_URL = "/browse/"; // Use to frame item level deeplink url when its category entry doesnt exist.
    public static final String CONFIGURE = "/configure";
    public static final String DEEPLINK_URL_CART = "/cart";
    private static final String ORDER_HISTORY_URL = "/order-history/";
    public static final String BASE_DEEPLINK_CURATED_URL = "/curated/"; //for Accessories deeplink url
    public static final String MAINTENANCE_PAGE_VIEW = "redirect:/ui/maintenance";
    private static final String DEEPLINK_WEB_SHOP_URL = "/webshop/";

    private static final Logger LOG = LoggerFactory.getLogger(ValidateLoginController.class);
    public static final String REDIRECT = "redirect:";

    @Autowired
    @Qualifier("productServiceV3Service")
    ProductServiceV3 productServiceV3;
    @Autowired
    CategoryConfigurationService categoryConfigurationService;
    @Autowired
    DomainVarMappingService domainVarMappingService;
    @Autowired
    private OrderHistoryService orderHistoryService;
    @Value("${image.server.url}")
    private String imageServerURL;


    @Autowired
    private OAuthConfig oauthConfig;

    @Autowired
    private VarOrderManagerHolder varOrderManagerHolder;
    @Autowired
    private ProgramService programService;
    @Autowired
    private Properties applicationProperties;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private CouponCodeValidator couponCodeValidator;
    @Autowired
    private CitiUtil citiUtil;
    @Autowired
    private VarProgramMessageService varProgramMessageService;

    @Autowired
    private CartService cartService;

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    @Autowired
    private ImageServerVersionService imageServerVersionService;

    @Autowired
    private MaintenanceMessageService maintenanceMessageService;

    private enum AnalyticsType {
        MATOMO("matomo"),
        TEALIUM("tealium"),
        ENSIGHTEN("ensighten"),
        WEBTRENDS("webtrends"),
        HEAP("heap");

        private final String value;

        AnalyticsType(final String value) {
            this.value = value;
        }
    }
    @PostConstruct
    public void setImageServerURL() {
        servletContext.setAttribute(CommonConstants.IMAGE_SERVER_KEY, applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY));
        servletContext.setAttribute(CommonConstants.IMAGE_SERVER_BUILD_NUMBER, imageServerVersionService.getVersionWtihoutNetworkCall());
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/customer/user" ,method = RequestMethod.GET)
    @ResponseBody
    public User browse(final HttpServletRequest servletRequest, final HttpServletResponse response) {
        XSSRequestWrapper request = new XSSRequestWrapper(servletRequest);
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        LOG.info("Remote ip address via request.getRemoteAddr() : {}", request.getRemoteAddr());
        LOG.info("Remote ip address via request.getHeader(\"X-FORWARDED-FOR\") : {}", request.getHeader("X-FORWARDED-FOR"));
        try {
            if (UserUtil.isSessionTimeout(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return null;
            }
            final User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
            Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final String ipAddress = getClientIpAddress(request);

            user.setIPAddress(ipAddress);
            if (program == null) {
                program = programService.getProgram(user.getVarId(), user.getProgramId(), user.getLocale());
            }
            getImageKey(program);
            request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
            final VAROrderManagerIF varOrderManager = getVAROrderManager(user.getVarId());
            final boolean isValid = varOrderManager.isValidLogin(user, program);

            if (isValid) {
                user.setImageURL(imageServerURL);
                if (program != null && program.getTargetCurrency() == null && user.getLocale() != null) {
                    program.setTargetCurrency(CurrencyUnit.of(
                            Currency.getInstance(user.getLocale()).getCurrencyCode().toUpperCase()));
                }
                return user;
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid program/pricing information");
                return null;
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Exception retrieving user from session", e);
        }
    }

    @RequestMapping(value = "/program" ,method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getProgram( final HttpServletRequest request) {
        Program program = null;
        try {
            program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);

            if (Objects.isNull(user)) {
                return new ResponseEntity<Object> (HttpStatus.UNAUTHORIZED);
            }

            if (program == null) {
                program = programService.getProgram(user.getVarId(), user.getProgramId(), user.getLocale());
            }
                if (program != null) {
                    getImageKey(program);
                    request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
                } else {
                    LOG.error("Unable to find active program having var id: {} and program id: {}", user.getVarId(), user.getProgramId());
                    return new ResponseEntity("Unable to find program having var id: " + user.getVarId() + " and program id: " + user.getProgramId(), HttpStatus.NO_CONTENT);
                }
            setBuildId(program);
            Object showFromPriceObj = program.getConfig().get(CommonConstants.SHOW_FROM_PRICE);
            Boolean showFromPrice = (showFromPriceObj != null && StringUtils.isNotBlank(showFromPriceObj.toString())) ? new Boolean(showFromPriceObj.toString()) : false;
            if (showFromPrice) {
                setCategoryPrice(request, user, program);
            }
            return new ResponseEntity<>(program, HttpStatus.OK);
        } catch (final Exception e) {
            LOG.error("Error loading program ", e);
            return new ResponseEntity<>("Unable to load program ", HttpStatus.NO_CONTENT);
        }
    }

    private void setCategoryPrice(final HttpServletRequest request, final User user, final Program program) throws ServiceException {
        Cart cart = (Cart) request.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        if (cart != null && user != null) {
            user.setDiscounts(cart.getDiscounts());
        }
        Collection<CategoryPrice> categoryPrices = productServiceV3.getCategoryPrices();
        if (CollectionUtils.isEmpty(categoryPrices)) {
            productServiceV3.populateCategoryPrices();
            categoryPrices = productServiceV3.getCategoryPrices();
        }
        if (CollectionUtils.isNotEmpty(categoryPrices) && user != null) {
            user.setDiscounts(couponCodeValidator.removeInvalidDiscount(user, user.getDiscounts()));
            List<CategoryPrice> categoryPriceList = productServiceV3.applySubsidyDiscountToCategoryPrices(categoryPrices, user.getDiscounts());
            productServiceV3.validateForPayrollEligibility(user, categoryPriceList, program, false);
            program.setCategoryPrices(categoryPriceList);

        }

    }

    @RequestMapping(value = "/signOut",method = RequestMethod.GET)
    public ResponseEntity<String> signOut(final HttpServletRequest request) throws Exception {
        try {
            final String signOutURL = StringUtils.trimToNull(CommonConstants.getExternalUrl(request, ExternalUrlConstants.VAR_SIGN_OUT_URL));
            String locale = "";
            if (request.getAttribute(CommonConstants.LOCALE) != null) {
                locale = (String) request.getAttribute(CommonConstants.LOCALE);
            }

            request.getSession().invalidate();
            if (signOutURL != null) {
                final HttpHeaders headers = new HttpHeaders();
                headers.add("Location", signOutURL + "&locale=" + locale);
                return new ResponseEntity<>(null,headers, HttpStatus.FOUND);
            }
        } catch (final Exception e) {
            LOG.error("Error while signing out", e);
            return new ResponseEntity<>("Error while signing out", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        request.getSession().invalidate();
        return new ResponseEntity<>("User sucessfully signed out", HttpStatus.OK);
    }


    /**
     * /signOutPage.htm will be used by payment server to logout WebApp and
     * the VAR specific sign-out flow.
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/signOutPage",method =RequestMethod.GET)
    public String signOutPage(final HttpServletRequest request) throws Exception {
        //Get the URL from Session
        final Map<String, String> urlMap = (Map<String, String>) request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);

        //Invalidate Session
        request.getSession().invalidate();

        //Check if the VAR specific URL has been provided and if provided,
        // redirect to the VAR specific signout flow.
        if (urlMap != null) {
            final String signOutURL = urlMap.get(ExternalUrlConstants.SIGN_OUT_URL);
            if (StringUtils.isNotBlank(signOutURL)) {
                return REDIRECT + signOutURL;
            }
        }
        return LOGIN_VIEW_WITH_RETURN;
    }

    @RequestMapping(value = "/ssoLoginAction",method = RequestMethod.GET)
    public String ssoLogin(final HttpServletRequest request) throws Exception {

        final String urlParam = AppleUtil.getURLParameterFromRequest(request);
        SessionUtil.restartSession(request);
        //Redirecting to same URL to re-login SAML as a new user
        //we have configured ssoLoginAction.do for SAML in rewardstep-servlet.xml
        return "redirect:ssoLoginAction.do" + urlParam;
    }

    @RequestMapping(value = "/ValidateLoginAction",method = RequestMethod.POST)
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        SessionUtil.restartSession(request);
        return processLogin(request);
    }

    @RequestMapping(value = "/oauthLoginAction",method = RequestMethod.GET)
    public String oauthLogin(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        SessionUtil.restartSession(request);
        return processLogin(request);
    }

       /*
    This method is used to call OAuth server to get access code
     */

    @RequestMapping(value = "/oauthAccessCode",method = RequestMethod.GET)
    public String getOAuthAccessCode(final HttpServletRequest request) throws Exception {
        final String varId=request.getParameter(OAuthRequestParam.VARID.getValue());
        final String okta = request.getParameter(OAuthRequestParam.OKTA.getValue());
        OAuthCredentials credentials = null;
        if(StringUtils.equalsIgnoreCase(okta,CommonConstants.YES_VALUE)){
            credentials = oauthConfig.getOAuthCredentials(OAuthRequestParam.OKTA.getValue());
        }else{
            credentials = oauthConfig.getOAuthCredentials(varId);
        }
        if(Objects.nonNull(credentials)){
            final String redirectUrl = AppleUtil.getOAuthRedirectURI(request,credentials.getRedirectUri());
            final URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.addParameter(EnumOAuthInputParameter.RESPONSE_TYPE.getValue(),OAuthRequestParam.CODE.getValue())
                    .addParameter(EnumOAuthInputParameter.AUTH_STATE.getValue(),UUID.randomUUID().toString())
                    .addParameter(EnumOAuthInputParameter.CLIENT_ID.getValue(),credentials.getClientId())
                    .addParameter(EnumOAuthInputParameter.SCOPE.getValue(),credentials.getScope())
                    .addParameter(EnumOAuthInputParameter.REDIRECT_URI.getValue(),redirectUrl);

            final String returnStr = REDIRECT +credentials.getAuthUrl()+uriBuilder.toString();
            LOG.info("oauthAccessCode URL: {}",returnStr);
            return returnStr;
        }else{
            return FAIL_VIEW;
        }
    }

    /**
     * 11-17-2021
     * D-18812 - Checkmarx : Java: Frameable Login Page
     * This endpoint is not used anymore after removing all jsp files
     * The below code can be deleted later
     *
    @RequestMapping(value = "/widgetLoginAction",method = RequestMethod.GET)
    public String widgetLogin(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if(SessionUtil.isSessionTimeout(request)){
            //Initiate the whole login again.
            oauthLogin(request, response);
            request.getSession().setAttribute("reloadLandingPage", true);
            return WIDGET_VIEW;
        } else{
            User user = (User)request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
            String onlineAuthCode = request.getParameter(OAuthRequestParam.ONLINE_AUTH_CODE.getValue());
            String offlineAuthCode = request.getParameter(OAuthRequestParam.OFFLINE_AUTH_CODE.getValue());
            if(StringUtils.isNotBlank(onlineAuthCode)
                    && !onlineAuthCode.equalsIgnoreCase(CommonConstants.NULL_VALUE)
                    && StringUtils.isNotBlank(offlineAuthCode)
                    && !offlineAuthCode.equalsIgnoreCase(CommonConstants.NULL_VALUE)) {
                final Map<String, String> additonalInfo =  user.getAdditionalInfo();
                additonalInfo.put(CommonConstants.ONLINE_AUTH_CODE, onlineAuthCode);
                additonalInfo.put(CommonConstants.OFFLINE_AUTH_CODE, offlineAuthCode);
                additonalInfo.put(CommonConstants.LOGIN_TIME, new SimpleDateFormat(CommonConstants.DATE_TIME_FORMAT).format(new Date()));
                user.setAdditionalInfo(additonalInfo);
            } else {
                request.getSession().invalidate();
            }
            return WIDGET_VIEW;
        }
    }
    */

    @RequestMapping(value = "/getXSRFToken", method = GET)
    @ResponseBody
    public String getXSRFTokenFromSession(final HttpServletRequest httpServletRequest, final HttpServletResponse response)
        throws UnsupportedEncodingException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
        final String loginXsrfToken = (String)request.getSession().getAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY);
        if (null == loginXsrfToken) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "UNAUTHORIZED";
        }
        response.setStatus(HttpStatus.OK.value());
        response.setHeader(XSRF_TOKEN_REQUEST_HEADER_KEY, loginXsrfToken);
        return "OK";
    }

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String processLoginGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException{
        return   processLogin(request);
    }
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String processLoginPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException{
        return   processLogin(request);
    }

    private String processLogin(final HttpServletRequest httpServletRequest) throws ServletException {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final String userId = request.getParameter("userid");
        final String sId = request.getParameter("sid");
        final String browseOnly = request.getParameter("browseonly");
        String varId = getVarId(request);
        // Capture image server version no at every login, so image server can be bumped up to a new version
        // without restarting apple-gr.
        servletContext.setAttribute(CommonConstants.IMAGE_SERVER_BUILD_NUMBER, imageServerVersionService.getVersion());

        if (userId == null && sId == null && browseOnly == null && varId == null) {
            return LOGIN_VIEW;
        }

        LOG.info("Trying to get VOM for var id: {}", varId);
        final VAROrderManagerIF varOrderManager = getVAROrderManager(varId);
        User user;
        try{
            user = varOrderManager.selectUser(request);
        }catch (final Exception e){
            LOG.error("processLogin : Error in select user. please find trace below",e);
            return getErrorView(request, e);
        }

        if (user == null) {
            LOG.warn("VAROrderManager.selectUser returned null for varId={}", varId);
            return FAIL_VIEW;
        }

        final String maintenanceMessage =
            maintenanceMessageService.getMaintenanceMessage(user.getVarId(), user.getProgramId());

        //If Maintenance Message is configured for the Var and Program, redirect the user to Maintenance Page
        if (StringUtils.isNotBlank(maintenanceMessage)) {
            request.getSession().setAttribute(MAINTENANCE_MESSAGE_SESSION_OBJECT, maintenanceMessage);
            return MAINTENANCE_PAGE_VIEW;
        }

        // Check VIP against var program restriction mapping
        if(isURLRestricted(user,request)){
            return FAIL_VIEW;
        }
        else{
            logSessionBehavior(user);
        }

        return populateRequest(request, user);
    }

    /**
     *
     * @param request
     * @param e
     * @return
     */
    private String getErrorView(XSSRequestWrapper request, Exception e) {
        if(e.getMessage().contains(CommonConstants.BLACKLIST)) {
            return BLACKLIST_VIEW;
        }else if(e.getMessage().contains(CommonConstants.DRP_IN_ACTIVE)){
            return MAINTENANCE;
        }else if(e.getMessage().contains(CommonConstants.INVALID_DISCOUNT_CODE)){
            String locale = "en_US";
            if (Objects.nonNull(request.getParameter(CommonConstants.LOCALE))) {
                locale = request.getParameter(CommonConstants.LOCALE);
            } else if (Objects.nonNull(request.getAttribute(CommonConstants.LOCALE))) {
                locale = request.getAttribute(CommonConstants.LOCALE).toString(); // SAML flow
            }
            return DISCOUNT_ERROR_VIEW + "?locale=" + locale;
        }
        else{
            return FAIL_VIEW;
        }
    }

    private void logSessionBehavior(final User user) {
        String sessionBehavior;
        if(user.isAnonymous()){
            sessionBehavior= CommonConstants.ANONYMOUS_USER_ID;
        }
        else if(user.isBrowseOnly()){
            sessionBehavior=CommonConstants.BROWSE_ONLY;
        }
        else{
            sessionBehavior= CommonConstants.NORMAL_LOGIN;
        }

        LOG.info("Session behaviour : {}" , sessionBehavior);
    }

    private String getAnalyticsServerEndpoint(final String varId, final String analyticsName) {
        String serverEndpoint =
            applicationProperties.getProperty(varId.toLowerCase() + DOT + analyticsName + DOT_ENDPOINT);
        return StringUtils.isNotBlank(serverEndpoint) ? serverEndpoint :
            applicationProperties.getProperty(analyticsName + DOT_ENDPOINT);
    }

    private String populateRequest(final HttpServletRequest request, final User user) {
        final Locale appLocale = setupAppLocale(request, user);
        user.setLocale(appLocale);
        LOG.debug("set user {} locale to {}", user.getUserId(), user.getLocale());

        final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        //S-06941:Disable Cart Persistence for DRP program
        if (CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase((String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE))) {
            cartService.emptyCart(user);
        }

        if (Objects.nonNull(program.getConfig().get(CommonConstants.ANALYTICS))) {
            final String analyticsConfig = (String) program.getConfig().get(CommonConstants.ANALYTICS);
            LOG.debug("Analytics config {}", analyticsConfig);

            if (!analyticsConfig.equalsIgnoreCase(CommonConstants.DISABLED)) {
                updateAnalyticsConfig(user, program, analyticsConfig);
            }
        }

        user.setHostName(AppleUtil.getHostName(request));
        request.getSession().setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        setFive9ConfigurationInSessionConfig(user, program);

        addXsrfTokenToSession(request, program);

        String successView = getSuccessView(request, program, user);

        final String customLandingPageUrl = (String) program.getConfig().getOrDefault("landing_page_url", "");
        final Optional<String> deepLinkUrl = getDeepLinkedUrl(request, program, user);
        if (deepLinkUrl.isPresent()) {
            LOG.info("Redirecting to deepLinkUrl : {}", deepLinkUrl);
            successView = successView + deepLinkUrl.get();
        } else {
            if (StringUtils.isNotBlank(customLandingPageUrl)) {
                LOG.info("Redirecting to customLandingPageUrl : {}", customLandingPageUrl);
                successView = successView + customLandingPageUrl;
            }
        }

        if (request.getAttribute(CommonConstants.CITI_USER_CONSENT) != null) {
            return REDIRECT + CitiNavigationTarget.CONSENT.getPath();
        }

        if ((deepLinkUrl.isEmpty()) && Objects.nonNull(request.getAttribute(CommonConstants.CITI_RELAY_STATE))) {
            return REDIRECT + request.getAttribute(CommonConstants.CITI_RELAY_STATE);
        }

        final String analyticsQueryParam = (String) request.getAttribute(CommonConstants.ANALYTICS_URL);
        if (StringUtils.isNotBlank(analyticsQueryParam)) {
            if (successView.endsWith(CommonConstants.SLASH)) {
                successView = StringUtils.chop(successView);
            }
            successView = UriComponentsBuilder.fromUriString(successView)
                    .query(analyticsQueryParam).build().toString();
        }

        return AppleUtil.getDeeplinkForAngular(successView);
    }

    private void addXsrfTokenToSession(HttpServletRequest request, Program program) {
        // Add CSRF token to session
        boolean setNewXsrfTokenFlag = AppleUtil.checkXsrfTokenReUse(program);
        if (setNewXsrfTokenFlag) {
            request.getSession().setAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY, UUID.randomUUID().toString());
        } else {
            reUseXsrfToken(request);
        }
    }

    private String getSuccessView(final HttpServletRequest request, final Program program, final User user) {
        String successView = SUCCESS_VIEW;
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIEW_ANONYMOUS_ORDER_DETAIL)) {
            final String uid = request.getParameter("uid");
            if (StringUtils.isNotBlank(uid)) {
                Optional<String> orderHistoryUrl = getOrderHistoryRedirect(uid, program, user);
                if (orderHistoryUrl.isPresent()) {
                    LOG.info("Redirecting to orderHistoryUrl : {}", orderHistoryUrl);
                    successView = successView + orderHistoryUrl.get();
                } else {
                    successView = FAIL_VIEW;
                }
            }
        }
        return successView;
    }

    private void updateAnalyticsConfig(User user, Program program, String analyticsConfig) {
        final List<String> analyticsList = Arrays.stream(analyticsConfig.split(CommonConstants.COMMA))
                .map(String::trim)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(analyticsList)) {
            LOG.info("List of Analytics: {}", analyticsList);
            analyticsList.forEach(analyticsName -> {
                if (EnumUtils.isValidEnum(AnalyticsType.class, analyticsName.toUpperCase())) {
                    program.getConfig()
                            .put(analyticsName + ENDPOINT,
                                    getAnalyticsServerEndpoint(program.getVarId(), analyticsName));

                    //for matomo alone site Id is required
                    if (analyticsName.equalsIgnoreCase(AnalyticsType.MATOMO.value)) {
                        program.getConfig().put(CommonConstants.ANALYTICS_MATOMO_SITEID, getMatomoSiteID(user));
                    }

                    //for heap analytics, app Id is required
                    if (analyticsName.equalsIgnoreCase(AnalyticsType.HEAP.value)) {
                        populateHashedUserId(user, program);
                        program.getConfig().put(CommonConstants.ANALYTICS_HEAP_APPID, getHeapAppID());
                    }
                }
            });
        }
    }

    private String getMatomoSiteID(User user){
        final Properties dbProperties =
                varProgramMessageService.getMessagesBasedOnCodeType(
                        Optional.ofNullable(user.getVarId()),
                        Optional.ofNullable(user.getProgramId()),
                        user.getLocale().toString(),
                        CommonConstants.ANALYTICS);

        if (Objects.nonNull(dbProperties) && !dbProperties.isEmpty()) {
            return dbProperties.getProperty(CommonConstants.MATOMO_SITE_ID);
        }
        return null;
    }

    private String getHeapAppID(){
        return applicationProperties.getProperty(CommonConstants.HEAP_APP_ID);
    }

    private void setFive9ConfigurationInSessionConfig(final User user, final Program program) {
        Five9Config five9Config = new Five9Config();

        final Boolean isChatEnable = Boolean.parseBoolean(String.valueOf(program.getConfig().get(CommonConstants.ENABLE_FIVE9_CHAT)));
        final Boolean isAnonymous = Boolean.parseBoolean(String.valueOf(program.getConfig().get(CommonConstants.CITI_LOGIN_REQUIRED)));
        five9Config.setChatEnable(isChatEnable && !isAnonymous && !user.isBrowseOnly());
        if (five9Config.isChatEnabled()) {
            final String five9Title = (String) varProgramMessageService.getMessages(Optional.of(user.getVarId()),
                    Optional.of(user.getProgramId()), getLocal(user)).getOrDefault(FIVE9_TITLE_KEY, FIVE9_TITLE_DEFAULT);
            five9Config.setRootUrl(applicationProperties.getProperty(CommonConstants.FIVE9_ROOT_URL_KEY));
            five9Config.setType(applicationProperties.getProperty(CommonConstants.FIVE9_TYPE_KEY));
            five9Config.setTenant(applicationProperties.getProperty(CommonConstants.FIVE9_TENANT_KEY));
            five9Config.setProfile(applicationProperties.getProperty(CommonConstants.FIVE9_PROFILE_KEY));
            five9Config.setTitle(five9Title);
        }

        Map<String, Object> sessionConfigs = new HashMap<>();
        sessionConfigs.put(CommonConstants.FIVE9_CONFIG, five9Config);
        setSessionConfig(program, sessionConfigs);
    }

    private String getLocal(final User user) {
        if (user.getLocale() != null) {
            return user.getLocale().toString();
        } else {
            return Locale.US.toString();
        }
    }

    private Optional<String> getDeepLinkedUrl(final HttpServletRequest request, final Program program, final User user) {
        final String citiRelayState = (String) request.getAttribute(CommonConstants.CITI_RELAY_STATE);
        final String relayState =
                StringUtils.isNotBlank(citiRelayState) ? citiRelayState : request.getParameter(SAML_RELAY_STATE);

        if (StringUtils.isNotBlank(relayState)) {
            LOG.info("RelayState for Deeplink: {}", relayState);

            //for SAML login
            if (StringUtils.isNotBlank(relayState) && StringUtils.contains(relayState, CommonConstants.QUESTION)) {
                final String[] params = StringUtils
                        .split(StringUtils.substringAfter(relayState, CommonConstants.QUESTION), CommonConstants.AND);
                Optional<String> customDeepLinkUrl = getCustomDeepLinkUrl(program, user, params);
                if (customDeepLinkUrl.isPresent()) {
                    return customDeepLinkUrl;
                }

                Optional<String> itemOrCategoryDeepLinkUrl = getItemOrCategoryDeepLinkUrl(program, user, params);
                if (itemOrCategoryDeepLinkUrl.isPresent()) {
                    return itemOrCategoryDeepLinkUrl;
                }
            }
        }
        //for unauth user
        return getDeeplinkUrlForUnAuthUser(request, program, user);
    }

    private Optional<String> getCustomDeepLinkUrl(Program program, User user, String[] params) {
        final String supplier = getValueFromParams(params, CommonConstants.SUPPLIER);
        final String page = getValueFromParams(params, CommonConstants.PAGE);
        final String shop = getValueFromParams(params, CommonConstants.SHOP);
        final String varProgramWebShops = (String) program.getConfig().get(CommonConstants.ACTIVE_WEB_SHOPS);

        //Checking page 'orderHistory' to redirect to Order History Page
        if (CommonConstants.ORDER_HISTORY_PAGE.equalsIgnoreCase(page)) {
            return Optional.of(ORDER_HISTORY_URL);
        }

        //Checking shop to redirect to Webshop Page
        if (isValidWebShop(varProgramWebShops, shop, program, user.getLocale())) {
            return Optional.of(DEEPLINK_WEB_SHOP_URL + shop);
        }

        //Checking supplier 'applecart' to redirect cart page
        if (CommonConstants.APPLE_SUPPLIER_DEEPLINK_CART.equalsIgnoreCase(supplier)) {
            return Optional.of(DEEPLINK_URL_CART);
        }
        return Optional.empty();
    }

    private Optional<String> getItemOrCategoryDeepLinkUrl(Program program, User user, String[] params) {
        String deepLinkUrl;
        final String itemId = getValueFromParams(params, CommonConstants.ITEM_ID);
        if (StringUtils.isNotBlank(itemId)) {   //Checking supplier 'apple' is not needed while redirecting from Core
            deepLinkUrl = getItemLevelDeepLinkURL(itemId, program, user);
            if (StringUtils.isNotBlank(deepLinkUrl)) {
                return Optional.of(deepLinkUrl);
            }
        }

        final String categoryOrProduct =
                getCategoryOrProduct(params, CommonConstants.CATEGORY, CommonConstants.PRODUCT);
        if (StringUtils.isNotBlank(categoryOrProduct)) {
            deepLinkUrl = getCategoryLevelDeepLinkURL(categoryOrProduct, user.getLocale(), program, false);
            if (StringUtils.isNotBlank(deepLinkUrl)) {
                return Optional.of(deepLinkUrl);
            }
        }
        return Optional.empty();
    }

    private Optional<String> getOrderHistoryRedirect(String reqUid, Program program, User user) {
        Optional<String> orderHistoryUrl = Optional.empty();
        try {
            final String uid = new String(Base64.getDecoder().decode(reqUid));
            if (StringUtils.isNotBlank(uid)) {
                final StringBuilder url = new StringBuilder();

                // uid = orderId|email
                final String[] splittedUid = uid.split("\\|");
                if (!orderHistoryService
                    .updateUserIdIfOrderExist(splittedUid[0], splittedUid[1], program, user.getLocale(), user)) {
                    LOG.error("getOrderHistoryDetails: B2S Order ID not found / does not belongs to UID # {}", uid);
                    return orderHistoryUrl;
                }
                url.append(ORDER_HISTORY_URL);
                url.append(splittedUid[0]);
                orderHistoryUrl = Optional.of(url.toString());
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Anonymous OrderHistoryDetails: Invalid UID # {} ", reqUid, e);
        } catch (RuntimeException e) {
            LOG.error("Anonymous OrderHistoryDetails: Error retrieving order details from DB for UID # {} ", reqUid, e);
        }
        return orderHistoryUrl;
    }


    /**
     * method to check shop available in VPC - activeWebShops, Check Shop value is not Blank , Check varProgram
     * WebShops Not Blank and not Hyphen
     *
     * @param varProgramWebShops
     * @param shop
     * @return true if Web Shop is configured in VPC
     */
    private boolean isValidWebShop(final String varProgramWebShops, final String shop, final Program program,
        final Locale locale) {
        if (StringUtils.isNotBlank(shop) && StringUtils.isNotBlank(varProgramWebShops)
            && !StringUtils.equals(varProgramWebShops, HYPHEN)
            && Stream.of(varProgramWebShops.split(",")).anyMatch(webShop -> webShop.equalsIgnoreCase(shop))) {

            final ProductSearchRequest productSearchRequest =
                productServiceV3.getProductSearchRequestBuilder(null, null, null, null, locale, null, null, null, program, shop,
                        false, false, null).build();

            final ProductSearchResponse productSearchResponse = productServiceV3.searchProducts(productSearchRequest);

            if (Objects.nonNull(productSearchResponse)) {
                final ProductSearchDocumentGroup documentGroup = productSearchResponse.getDefaultGroup().orElse(null);
                if (Objects.nonNull(documentGroup) && documentGroup.getTotalFound() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<String> getDeeplinkUrlForUnAuthUser(final HttpServletRequest request, final Program program, final User user){

        final String addtnlRequestParams = request.getParameter("additionalInfo");
        if (StringUtils.isNotBlank(addtnlRequestParams)) {
            String deepLinkUrl = null;
            final Map<String, String> addtnlParams = Splitter.on(" ")
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator(":")
                    .split(addtnlRequestParams);
            final String product = addtnlParams.get(CommonConstants.PRODUCT);
            final String itemId = addtnlParams.get(CommonConstants.ITEM_ID);
            LOG.info("Deeplink for unauth - product - {}", product);
            LOG.info("Deeplink for unauth - itemId - {}", itemId);

            //category level deep linking
            if (StringUtils.isNotBlank(product)) {
                deepLinkUrl = getCategoryLevelDeepLinkURL(product, user.getLocale(), program, false);
                if (StringUtils.isNotBlank(deepLinkUrl)) {
                    return Optional.ofNullable(deepLinkUrl);
                }
            }

            //sku level deep linking
            if (StringUtils.isNotBlank(itemId)) {
                deepLinkUrl = getItemLevelDeepLinkURL(itemId, program, user);
                if (StringUtils.isNotBlank(deepLinkUrl)) {
                    return Optional.ofNullable(deepLinkUrl);
                }

            }//end
        }
        return Optional.empty();
    }

    private String getVarId(HttpServletRequest request) {
        String varId = null;
        LOG.info("Server name - {}", request.getServerName());
        final DomainVarMapping userInfo = domainVarMappingService.findDomainByPattern(CommonConstants.SAML,request.getServerName());
        if(userInfo != null) {
            varId = userInfo.getVarId();
        }

        if(StringUtils.isEmpty(varId)) {
            if (StringUtils.isNotBlank(request.getParameter("varId"))) {
                varId = request.getParameter("varId");
            } else {
                varId = request.getParameter("varid");
            }
        }
        return varId;
    }

    private Locale setupAppLocale(final HttpServletRequest request, final User user) {
        String locale = "";
        if (request.getAttribute(CommonConstants.LOCALE) != null) {
            locale = (String) request.getAttribute(CommonConstants.LOCALE);
        }

        if (StringUtils.isBlank(locale)) {
            locale = request.getParameter("locale");
        }
        if(StringUtils.isBlank(locale) && user.getLocale() != null){
            locale = user.getLocale().toString();
        }

        if (StringUtils.isBlank(locale) ) {
            locale = "en_US";
        }

        Locale appLocale = null;
        if (StringUtils.isNotBlank(locale)) {
            appLocale = LocaleUtils.toLocale(locale.trim());
        }
        Config.set(request.getSession(), Config.FMT_LOCALE, appLocale);
        request.getSession().setAttribute("locale", locale);
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, appLocale);
        return appLocale;
    }

    private VAROrderManagerIF getVAROrderManager(final String varId) {
        try {
            return varOrderManagerHolder.getVarOrderManager(varId);
        } catch (final Exception e) {
            LOG.error((e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + " when loading VAR Order Manager for varId=" + varId, e);
            throw new RuntimeException("can't load VAR order manager for varId=" + varId, e);
        }
    }

    @RequestMapping(value = "/DomainLogin",method = RequestMethod.GET)
    public String domainLogin(final HttpServletRequest request)
            throws ServletException {
        final DomainVarMapping userinfo = domainVarMappingService.findDomainByPattern(CommonConstants.URL,request.getServerName());
        if (userinfo != null) {

            boolean hasSessionRestart = getSessionRestartFlag(userinfo.getVarId(),
                    userinfo.getProgramId());
            if(hasSessionRestart){
                LOG.info("Session Restarting...");
                SessionUtil.restartSession(request);
            }

            final StringBuilder url=new StringBuilder();
            url.append(CommonConstants.LOGIN_REDIRECT_URL );
            url.append(CommonConstants.VAR_ID).append('=').append(userinfo.getVarId()).append('&');
            url.append(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE).append('=').append(userinfo.getProgramId()).append('&');
            url.append(CommonConstants.USER_ID).append('=')
                    .append(CommonConstants.ANONYMOUS_USER_ID ).append(request.getSession().getId())
                    .append('&')
                    .append(CommonConstants.ANONYMOUS_FLAG).append('=')
                    .append(request.getParameter(CommonConstants.ANONYMOUS_FLAG));
            if(!request.getParameterMap().isEmpty()){
                url.append('&'+CommonConstants.ADDITIONAL_INFO+'=');
                request.getParameterMap().forEach((k,v)-> url.append(k+':'+request.getParameter(k) +' '));
            }

            return AppleUtil.getDeeplinkForAngular(url.toString());
        } else{
            return FAIL_VIEW;
        }
    }

    @RequestMapping(value = "/AnonLogin",method = RequestMethod.GET)
    public String anonymousLogin(@RequestParam(required = false, value = CommonConstants.ANONYMOUS_VAR_ID_REQ_PARAM) final String varId,
                                 @RequestParam(required = false, value = CommonConstants.ANONYMOUS_PROGRAM_ID_REQ_PARAM) final String programId,
                                 @RequestParam(required = false, value = CommonConstants.ANONYMOUS_LOCALE_REQ_PARAM) final String localeStr,
                                 @RequestParam(required = false, value = CommonConstants.ANONYMOUS_DISCOUNT_CODE_REQ_PARAM) final String discountCode,
                                 @RequestParam(required = false, value = CommonConstants.ANONYMOUS_UID_CODE_REQ_PARAM) final String uid,
                                 @RequestParam(required = false, value = CommonConstants.ADDITIONAL_INFO) final String additionalInfo,
                                 final HttpServletRequest request) throws ServletException {
        if(StringUtils.isBlank(varId) || StringUtils.isBlank(programId)) {
            return FAIL_VIEW;
        }
        final Locale locale = (StringUtils.isNotBlank(localeStr)) ? LocaleUtils.toLocale(localeStr) : Locale.US;
        final Optional<String> anonymousPurchaseOpt = programService.getProgramConfigValue(varId, programId, CommonConstants.ANONYMOUS_PURCHASE);
        final boolean anonymousPurchase = Boolean.valueOf(anonymousPurchaseOpt.orElse(CommonConstants.FALSE_VALUE));
        if (anonymousPurchase) {
            SessionUtil.restartSession(request);
            final StringBuilder url=new StringBuilder();
            url.append(CommonConstants.LOGIN_REDIRECT_URL);
            url.append(CommonConstants.VAR_ID).append('=').append(varId).append('&');
            url.append(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE).append('=').append(programId).append('&');
            url.append(CommonConstants.LOCALE).append('=').append(locale.toString()).append('&');
            if(StringUtils.isNotBlank(discountCode)) {
                url.append(CommonConstants.ANONYMOUS_DISCOUNT_CODE_REQ_PARAM).append('=').append(discountCode).append('&');
            }
            if(StringUtils.isNotBlank(uid)) {
                url.append(CommonConstants.ANONYMOUS_UID_CODE_REQ_PARAM).append('=').append(uid).append('&');
            }
            if(StringUtils.isNotBlank(additionalInfo)) {
                url.append(CommonConstants.ADDITIONAL_INFO).append('=').append(additionalInfo).append('&');
            }
            url.append(CommonConstants.USER_ID).append('=')
                    .append(CommonConstants.ANONYMOUS_USER_ID ).append(request.getSession().getId());
            return AppleUtil.getDeeplinkForAngular(url.toString());
        } else{
            return FAIL_VIEW;
        }
    }

    private boolean isURLRestricted(User user,HttpServletRequest request) {

        final boolean isRestricted;

        final List<DomainVarMapping> domainVarMappings = domainVarMappingService.findDomainsByPattern(CommonConstants.VIP,request.getServerName());


        if (CollectionUtils.isEmpty(domainVarMappings)) {
            isRestricted = false;
        } else {

            boolean exactMatchAvailable = domainVarMappings.stream().filter( domainVarMapping ->
                    domainVarMapping.getDomain().equalsIgnoreCase(request.getServerName())).findAny().isPresent();

            if(exactMatchAvailable){
                isRestricted = !domainVarMappings.stream()
                        .filter( domainVarMapping -> domainVarMapping.getDomain().equalsIgnoreCase(request.getServerName()))
                        .anyMatch(domainVarMapping1 -> domainVarMapping1.getVarId().equalsIgnoreCase(user.getVarId()) &&
                                domainVarMapping1.getProgramId().equalsIgnoreCase(user.getProgramId()));
            }else{
                isRestricted= !domainVarMappings.stream().filter(domainVarMapping -> {
                    Pattern p = Pattern.compile(domainVarMapping.getDomain());
                    Matcher m = p.matcher(request.getServerName());
                    return m.matches();
                }).anyMatch(domainVarMapping1 -> domainVarMapping1.getVarId().equalsIgnoreCase(user.getVarId()) &&
                        StringUtils.isBlank(domainVarMapping1.getProgramId()));
            }

            if(isRestricted){
                LOG.info("URL Domain {} not authorized to use varId [{}] programId [{}]",
                        request.getServerName(), user.getVarId(), user.getProgramId());
            }
        }
        return isRestricted;

    }

    private String getItemLevelDeepLinkURL(String itemId, final Program program, final User user) {
        String deepLinkURL = null;
        String skuId = StringUtils.replace(itemId, HYPHEN, CommonConstants.SLASH);
        if (!StringUtils.startsWith(skuId, CommonConstants.APPLE_MERCHANT_ID)) {
            skuId = CommonConstants.APPLE_MERCHANT_ID + skuId;
        }

        final Product productDetail = productServiceV3.getDetailPageProduct(skuId, program, user, false, false);

        if (Objects.nonNull(productDetail) && CollectionUtils.isNotEmpty(productDetail.getCategories())) {
            final List<Category> categories = productDetail.getCategories();
            final Category category = categories.get(0);
            final String categorySlug = category.getSlug();
            final String parentCategorySlug = category.getParents().get(0).getSlug();

            // Getting Deep Link URL from DB
            final String stringCategoryDeepLinkURL = getCategoryLevelDeepLinkURL(categorySlug, user.getLocale(), program, true);
            LOG.info("Item category level Deep link URL from DB {} for SKU ID {}", stringCategoryDeepLinkURL, itemId);

            deepLinkURL = buildItemDeepLinkURL(skuId, categorySlug, parentCategorySlug, stringCategoryDeepLinkURL);
        } else {
            LOG.warn("Unable to get Slug from Product details for SKU ID {}", itemId);
        }

        LOG.info("Final Item level Deep link URL {}", deepLinkURL);
        return deepLinkURL;
    }

    private String buildItemDeepLinkURL(String skuId, String categorySlug, String parentCategorySlug, String stringCategoryDeepLinkURL) {
        String deepLinkURL = null;
        if (Objects.nonNull(stringCategoryDeepLinkURL)) {
            // Not appending SKU ID if Deep link URL contains "configure"
            if (stringCategoryDeepLinkURL.contains(CONFIGURE)) {
                deepLinkURL = stringCategoryDeepLinkURL;
            } else {
                deepLinkURL = stringCategoryDeepLinkURL + StringUtils.replace(skuId, CommonConstants.SLASH, HYPHEN);
            }
        } else {
            if (StringUtils.isNotBlank(parentCategorySlug)) {
                final StringBuilder stringBuilder = new StringBuilder();
                if (AppleUtil.isAccessories(categorySlug)) {
                    stringBuilder.append(BASE_DEEPLINK_CURATED_URL);
                    stringBuilder.append(CommonConstants.ACCESSORIES).append(CommonConstants.SLASH);
                } else {
                    stringBuilder.append(BASE_DEEPLINK_URL);
                }
                stringBuilder
                        .append(parentCategorySlug).append(CommonConstants.SLASH)
                        .append(categorySlug).append(CommonConstants.SLASH)
                        .append(StringUtils.replace(skuId, CommonConstants.SLASH, HYPHEN));
                deepLinkURL = stringBuilder.toString();
            }
        }
        return deepLinkURL;
    }

    private String getCategoryLevelDeepLinkURL(final String product,final Locale locale,final Program program, boolean productValidated){
        final CategoryConfiguration categoryConfiguration = categoryConfigurationService.getCategoryConfiguration(product);
        if(categoryConfiguration != null) {
            if (productValidated){
                return getDeepLinkURLFromCateogryConfiguration(categoryConfiguration);
            }

            Set<String> categorySlugs = new HashSet<>();
            if (StringUtils.isNotEmpty(product)) {
                categorySlugs.add(product);
            }
            final ProductSearchRequest productSearchRequest =
                    productServiceV3.getProductSearchRequestBuilder(categorySlugs, null, null, null, locale, null, null,
                            null, program,null,false,false,null).build();
            final ProductSearchResponse productSearchResponse = productServiceV3.searchProducts(productSearchRequest);
            if (Objects.nonNull(productSearchResponse)) {
                final ProductSearchDocumentGroup documentGroup = productSearchResponse.getDefaultGroup().orElse(null);
                if (Objects.nonNull(documentGroup) && documentGroup.getTotalFound() > 0) {
                    return getDeepLinkURLFromCateogryConfiguration(categoryConfiguration);
                }
            }
        }
        return null;
    }

    private String getDeepLinkURLFromCateogryConfiguration(CategoryConfiguration categoryConfiguration) {
        final String deepLinkUrl = categoryConfiguration.getDeepLinkUrl();
        return StringUtils.isNotBlank(deepLinkUrl) ? deepLinkUrl : null;
    }

    private static String getCategoryOrProduct(String[] params,String categoryKey,String productKey){
        String category = getValueFromParams(params,categoryKey);
        String product = getValueFromParams(params,productKey);
        String categoryOrProduct = null;
        if(StringUtils.isNotBlank(category) || StringUtils.isNotBlank(product)){
            categoryOrProduct = StringUtils.isNotBlank(category)?category:product;
        }
        return categoryOrProduct;
    }

    private static String getValueFromParams(String[] params,String key){
        String value = Arrays.stream(params).filter(param -> StringUtils.containsIgnoreCase(param,key)).findAny()
                .orElse(null);
        return StringUtils.isNotBlank(value)?
                StringUtils.substringAfter(value,CommonConstants.EQUAL):null;
    }

    /**
     * This method is used to get the flag for session restart
     *
     * @param varId,programId
     * @return default true
     */
    private boolean getSessionRestartFlag(final String varId,final String programId){
        final VarProgramConfigEntity
                varProgramConfig = varProgramConfigDao.getVarProgramConfigByVarProgramName(varId, programId,
                CommonConstants.SESSION_RESTART);
        if(Objects.nonNull(varProgramConfig)){
            return BooleanUtils.toBoolean(varProgramConfig.getValue());
        }
        return Boolean.TRUE;
    }

    /**
     * Re-use Old Xsrf Token from the session
     *
     * @param request
     */
    private void reUseXsrfToken(final HttpServletRequest request){
        String loginXsrfToken =
                (String)request.getSession().getAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY);
        if(StringUtils.isNotBlank(loginXsrfToken)){
            request.getSession().setAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY, loginXsrfToken);
        }else{
            request.getSession().setAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY, UUID.randomUUID().toString());
        }
    }

    private void setBuildId(Program program)
    {
        String b2sVersion = (String) servletContext.getAttribute("B2S-Version");
        String buildNumber = (String) servletContext.getAttribute("Build-Number");
        Map<String, Object> sessionConfigs = new HashMap<>();
        sessionConfigs.put("buildId", b2sVersion + "-" + buildNumber);
        setSessionConfig(program,sessionConfigs);
    }
    private void getImageKey(Program program)
    {
        Map<String, Object> sessionConfigs = new HashMap<>();
        sessionConfigs.put( CommonConstants.IMAGE_SERVER_KEY, applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY));
        sessionConfigs.put(CommonConstants.IMAGE_SERVER_BUILD_NUMBER, imageServerVersionService.getVersionWtihoutNetworkCall());
        setSessionConfig(program,sessionConfigs);
    }
    private void setSessionConfig(Program program,Map<String, Object> sessionConfigs)
    {
        if(program.getSessionConfig()!=null)
            program.getSessionConfig().putAll(sessionConfigs);
        else
            program.setSessionConfig(sessionConfigs);
    }

    private void populateHashedUserId(final User user, final Program program) {
        final StringBuffer identifier =
            new StringBuffer()
                .append(program.getVarId()).append(CommonConstants.OPTIONS_JOIN_SEPARATOR)
                .append(program.getProgramId()).append(CommonConstants.OPTIONS_JOIN_SEPARATOR)
                .append(user.getUserId());
        user.setHashedUserId(generateHashCode(identifier.toString()));
    }

    private String generateHashCode(final String originalString) {
        String sha256hex = DigestUtils.sha256Hex(originalString);
        return sha256hex.substring(0, 32);
    }

}
