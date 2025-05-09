package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.rewards.apple.dao.DemoUserDao;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.security.oauth.*;
import com.b2s.security.oauth.service.OAuthTokenService;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.common.order.util.OAuthRequestParam;
import com.google.common.base.MoreObjects;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.replaceNullString;

/**
 * Created by skither on 7/25/2019.
 */
@Component("varOrderManagerVirginAU")
public class VAROrderManagerVirginAU extends GenericVAROrderManager  {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerVirginAU.class);

    @Autowired
    private HttpServletRequest httpServletRequest;

    private static final String VAR_ID = "VirginAU";

    private static final String PROGRAM_ID_DEFAULT = "b2s_qa_only";

    @Value("${oauth.demo.user}")
    private String demoUser;

    @Value("${oauth.demo.pwd}")
    private String password;

    @Autowired
    private OAuthTokenService oAuthTokenService;

    @Autowired
    private OAuthConfig oauthConfig;

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        User user = null;
        CommonConstants.LoginType loginType = null;
        try {
            if (StringUtils.isNotBlank(request.getParameter(OAuthRequestParam.CODE.getValue()))) {
                loginType = CommonConstants.LoginType.OAUTH;
                user = getUser(request);
                final String oktaFlag = request.getParameter(OAuthRequestParam.OKTA.getValue());

                if (StringUtils.equalsIgnoreCase(oktaFlag, CommonConstants.YES_VALUE)) {
                    LOGGER.info("Local Program sign using OAuth..");
                    user = createOAuthUserObject(request, user);
                    request.getSession().setAttribute(ExternalUrlConstants.PURCHASE_POST_URL,
                            applicationProperties.getProperty(ExternalUrlConstants.LOCAL_USER_PURCHASE_POST_URL));
                } else {
                    //VIS call to get user profile
                    if (isUserObjectCreationError(user)) {
                        return null;
                    }
                }
            } else  if(request.getParameter(CommonConstants.USER_ID).toLowerCase().contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())){
                user = populateUserForAnonymousLogin(request);
                loginType = CommonConstants.LoginType.ANONYMOUS;
            }
            else {
                user = selectLocalUser(request, new User());
                loginType = CommonConstants.LoginType.FIVEBOX;
                user.setPricingTier(request.getParameter(CommonConstants.PRICING_TIER));
                if (StringUtils.isNotBlank(user.getPricingTier())) {
                    user.getAdditionalInfo().put(CommonConstants.PRICING_TIER, user.getPricingTier());
                }
                //login for ANONYMOUS user to be added to User Object
                loginType = addAnonymousToUserObject(user, loginType);

            }
            initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_AU,CommonConstants.COUNTRY_CODE_AU);
            //Select program information from database
            final Program program = getProgram(user);
            if (Objects.nonNull(user)) {
                program.setPricingTier(user.getPricingTier());
            }
            if (MapUtils.isNotEmpty(program.getConfig()) &&
                    Objects.nonNull(program.getConfig().get(CommonConstants.SIGNIN_URL_KEY))) {
                StringBuilder sbSigniInUrl = new StringBuilder();
                sbSigniInUrl.append(AppleUtil.getHostName(request));
                sbSigniInUrl.append(program.getConfig().get(CommonConstants.SIGNIN_URL_KEY));
                program.getConfig().put(CommonConstants.SIGNIN_URL_KEY, sbSigniInUrl.toString());
            }
            updateAdditionalAttributesInSession(request, program, user, loginType);

            //Set Bag Menu Urls based on Precedence
            setBagMenuUrls(request, loginType);
            setDefaultCountryCode(user);
            setSessionTimeOut(request, user);
            prepareUserAddress(user);
            setOauthParam(program,request);
            request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        } catch (final RuntimeException e) {
            LOGGER.error("Error while user log in for VirginAU", e);
            return null;
        }

        return user;
    }

    private CommonConstants.LoginType addAnonymousToUserObject(User user, CommonConstants.LoginType loginType) {
        if(user.getProgramid().toLowerCase().equalsIgnoreCase(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())){
            user.setAnonymous(Boolean.TRUE);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        }
        return loginType;
    }

    private void setDefaultCountryCode(User user) {
        if (StringUtils.isBlank(replaceNullString(user.getCountry())) || !user.getCountry().equalsIgnoreCase(CommonConstants.COUNTRY_CODE_AU)) {
            LOGGER.info("VirginAU country defaulted to AU ");
            user.setCountry(CommonConstants.COUNTRY_CODE_AU);
        }
    }

    private User populateUserForAnonymousLogin(HttpServletRequest request) {
        User user = new User();
        user.setProgramId(request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE));
        user.setVarId(request.getParameter(CommonConstants.VAR_ID));
        user.setUserId(request.getParameter(CommonConstants.USER_ID));
        user.setAnonymous(true);
        return user;
    }

    private boolean isUserObjectCreationError(User user) {
        SessionResponse sessionResponse = varIntegrationServiceRemoteImpl.getUserSession(user);
        if (Objects.nonNull(sessionResponse)) {
            // createUserObject from VIS response
            LOGGER.info("SessionResponse retrived for user id = {} / varId = {}", user.getUserId(),user.getVarId());
            user.getAdditionalInfo().clear();//token needs to cleared
            createUserObject(sessionResponse, user);
        } else {
            LOGGER.error("SessionResponse from VIS is empty for user id = {} / varId = {}", user.getUserId(),user.getVarId());
            return true;
        }
        return false;
    }

    /**
     * Set Bag Menu Urls based on Precedence.
     * SAML response is having higher precedence
     * DB(var_program_config) is next level
     * Value for Application properties is having least preference
     */
    private void setBagMenuUrls(final HttpServletRequest request, final CommonConstants.LoginType loginType) {
        // Set Existing SignOut and NavigateBackUrl from Application Properties
        setSignOutUrl(request);
        setNavigateBackUrl(request);
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            final Message msg = new Message();
            //process points payment if applicable
            if (isInvalidRedemptionResponse(order, user, program)){
                return false;
            }
            //adjust user points
            getBalanceUserPoints(order, user);
            setMessageSuccess(order, msg);
            message = msg;

        } catch (final Exception ex) {
            placeOrderExceptionHandler(order, user, ex);
        }

        return message.isSuccess();
    }

    private boolean isInvalidRedemptionResponse(Order order, User user, Program program) throws Exception {
        RedemptionResponse redemptionResponse;// Points payment  - Sum cashBuyInPoints since it will be negative values
        final int totalPointsPaid = order.getOrderTotalInPoints() + order.getOrderTotalCashBuyInPoints();
        final boolean disableCashOnlyRedemption = (Boolean) program.getConfig().getOrDefault(CommonConstants.DISABLE_CASH_ONLY_REDEMPTION, Boolean.FALSE);

        if(!(disableCashOnlyRedemption && totalPointsPaid <= 0)) {
            redemptionResponse = performRedemption(order, user, program);

            if (redemptionResponse == null) {
                return true;
            }
            //assign/update order with the redemption response
            order.setVarOrderId(redemptionResponse.getVarOrderId());
        }else{
            // If disableCashOnlyRedemption is true we will not call VIMS and hence we dont get VAR_order_id.
            // But var_order_id is mandatory for PSA to process order and hence orderId is set to Var_order_id
            order.setVarOrderId(order.getOrderId().toString());
        }
        return false;
    }

    private RedemptionResponse performRedemption(Order order, User user, Program program) throws Exception {
        RedemptionResponse redemptionResponse;
        if (program != null && program.getIsLocal()) {
            //If demo user
            redemptionResponse = varIntegrationServiceLocalImpl.performRedemption(order, user, program);
        } else {
            //Redemption from VIS
            updateOAuthToken(httpServletRequest, user);
            redemptionResponse = varIntegrationServiceRemoteImpl.performRedemption(order, user, program);
        }
        return redemptionResponse;
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, Program program) {

        return performCancelRedemption(order, program);
    }

    @Override
    public int getUserPoints(final User user, Program program) throws B2RException {

        if (program != null && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            final Optional<String> sid = Optional.ofNullable(user.getSid());

            updateOAuthToken(httpServletRequest, user);

            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
        }

    }

    @Override
    protected User getUser(final HttpServletRequest request) {
        final User user = new User();

        final String varId = request.getParameter(OAuthRequestParam.VARID.getValue());
        final String memberId = request.getParameter(OAuthRequestParam.MEMBER_ID.getValue());

        final String code = request.getParameter(OAuthRequestParam.CODE.getValue());
        final String state = request.getParameter(EnumOAuthInputParameter.AUTH_STATE.getValue());

        final String okta = request.getParameter(OAuthRequestParam.OKTA.getValue());

        user.setVarId(StringUtils.isBlank(varId) ? VAR_ID : varId);

        //Dummy userID, VIS don't care about user id since we pass auth token
        user.setUserId(StringUtils.isBlank(memberId) ? "123456789" : memberId);
        OAuthCredentials credentials = null;
        if (StringUtils.equalsIgnoreCase(okta, CommonConstants.YES_VALUE)) {
            credentials = oauthConfig.getOAuthCredentials(OAuthRequestParam.OKTA.getValue());
        } else {
            credentials = oauthConfig.getOAuthCredentials(varId);
        }

        final String redirectUrl = AppleUtil.getOAuthRedirectURI(request, credentials.getRedirectUri());
        Token token = oAuthTokenService.getTokenFomCode(code, credentials, redirectUrl);

        OAuthAttributes oAuthAttributes = new OAuthAttributes();
        oAuthAttributes.setCode(code);
        oAuthAttributes.setState(state);
        oAuthAttributes.setToken(token);
        oAuthAttributes.setoAuthCredentials(credentials);
        request.getSession().setAttribute(CommonConstants.OAUTH_ATTRIBUTES, oAuthAttributes);
        request.getSession().setAttribute(CommonConstants.OAUTH_CHECK_SESSION_IFRAME_URL,credentials.getCheckSessionIframe());
        request.getSession().setAttribute(CommonConstants.OAUTH_CLIENT_ID, credentials.getClientId());
        request.getSession().setAttribute(CommonConstants.OAUTH_TOKEN_SESSION_STATE, token.getSessionState());

        final Map<String, String> additonalInfo = new HashMap();
        additonalInfo.put(CommonConstants.LOGIN_TIME, new SimpleDateFormat(CommonConstants.DATE_TIME_FORMAT).format(new Date()));
        additonalInfo.put(EnumOAuthInputParameter.AUTH_STATE.getValue(), UUID.randomUUID().toString());
        if (Objects.nonNull(token)) {
            final String accessToken = StringUtils.right(token.getAccessToken(), 10);
            LOGGER.info("VA AccessToken: (last 10 digit): {}", accessToken);
            additonalInfo.put(CommonConstants.AUTHORIZATION_TOKEN, token.getAccessToken());
        }
        user.setAdditionalInfo(additonalInfo);

        initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_AU,CommonConstants.COUNTRY_CODE_AU);
        return user;
    }

    public void createUserObject(final SessionResponse sessionResponse, final User user) {
        if (Objects.nonNull(sessionResponse.getAccountId())) {
            user.setUserId(sessionResponse.getAccountId());
        } else {
            LOGGER.error("We are not receiving AccountId...");
        }
        if (Objects.nonNull(sessionResponse.getSessionId())) {
            user.setSid(sessionResponse.getSessionId());
        }
        AccountInfo accountInfo = sessionResponse.getAccountInfo();
        populateUserObjFromAccountInfo(user, accountInfo);

    }

    private void populateUserObjFromAccountInfo(User user, AccountInfo accountInfo) {
        if (Objects.nonNull(accountInfo)) {
            final UserInformation userInfo = accountInfo.getUserInformation();
            user.setFirstName(userInfo.getFirstName());
            user.setLastName(userInfo.getLastName());
            user.setEmail(userInfo.getEmailAddresses().length > 0 ? userInfo.getEmailAddresses()[0].getEmail() : "");
            user.setPhone(userInfo.getPhoneNumbers().length > 0 ? (userInfo.getPhoneNumbers())[0].getNumber() : "");

            final Address address = userInfo.getAddress();

            if (Objects.nonNull(address)) {
                LOGGER.info("verify address part : {}", address);
                user.setAddr1(MoreObjects.firstNonNull(address.getLine1(), ""));
                user.setAddr2(MoreObjects.firstNonNull(address.getLine2(), ""));
                user.setCity(MoreObjects.firstNonNull(address.getCity(), ""));
                user.setZip(MoreObjects.firstNonNull(address.getPostalCode(), ""));
                user.setState(MoreObjects.firstNonNull(address.getStateCode(), ""));
                user.setCountry(MoreObjects.firstNonNull(address.getCountryCode(), ""));
            }
            accountInfo.getProgramId().ifPresentOrElse(progId -> user.setProgramId(progId),
                () -> LOGGER.error("We are not receiving program id for user id...  {}", user.getUserId()));

            if(Objects.nonNull(accountInfo.getUserInformation().getAdditionalInfo()) && !accountInfo.getUserInformation().getAdditionalInfo().isEmpty()) {
                user.getAdditionalInfo().putAll(accountInfo.getUserInformation().getAdditionalInfo());
                user.getAdditionalInfo().put(CommonConstants.PRICING_TIER, accountInfo.getPricingTier());
            }
            user.setPricingTier(accountInfo.getPricingTier());
            // Set points balance
            user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
        }
    }

    private void setSignOutUrl(final HttpServletRequest request) {
        //get existing external_urls
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);

        final String signOutUrl = getSignOutUrl(request);
        externalUrls.put(ExternalUrlConstants.SIGN_OUT_URL, signOutUrl);
        externalUrls.put(ExternalUrlConstants.TIME_OUT_URL, signOutUrl);
        externalUrls.put(ExternalUrlConstants.VAR_SIGN_OUT_URL, signOutUrl);
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }
    private void setNavigateBackUrl(final HttpServletRequest request) {
        //get existing external_urls
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL,
                applicationProperties.getProperty(CommonConstants.VIRGINAU_NAVIGATEBACKURL));
        //added for home link
        externalUrls.put(ExternalUrlConstants.HOME_LINK_URL,
                applicationProperties.getProperty(CommonConstants.VIRGINAU_HOMELINKURL));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private User createOAuthUserObject(final HttpServletRequest request, final User user) {
        try {
            user.setUserId(demoUser);
            user.setPassword(password);
            final String programId = request.getParameter(OAuthRequestParam.PROGRAM_ID.getValue());
            user.setProgramId(StringUtils.isBlank(programId) ? PROGRAM_ID_DEFAULT : programId);

            final User returnUser = getDemoUserFromDB(user, user.getProgramId(), user.getVarId(), demoUser, demoUserDao, password);;
            returnUser.setPricingTier(request.getParameter(CommonConstants.PRICING_TIER));
            if (StringUtils.isNotBlank(user.getPricingTier())) {
                returnUser.getAdditionalInfo().put(CommonConstants.PRICING_TIER, user.getPricingTier());
            }
            return returnUser;
        } catch (final Exception ex) {
            LOGGER.error("Unknown Error", ex);
        }
        return null;
    }

    public void updateOAuthToken(final HttpServletRequest servletRequest, final User user) {
        OAuthAttributes oAuthAttributes =
                (OAuthAttributes) servletRequest.getSession().getAttribute(CommonConstants.OAUTH_ATTRIBUTES);
        if (Objects.nonNull(oAuthAttributes)) {
            Token currentToken = oAuthAttributes.getToken();
            if (Objects.nonNull(currentToken)) {
                if (oAuthTokenService.isTokenValid(currentToken)) {
                    updateUserWithToken(user, currentToken);
                } else {
                    Token newToken = oAuthTokenService.refreshToken(oAuthAttributes);
                    updateUserWithToken(user, newToken);
                    oAuthAttributes.setToken(newToken);
                    servletRequest.getSession().setAttribute(CommonConstants.OAUTH_ATTRIBUTES, oAuthAttributes);
                }
            }
        }
    }

    private void updateUserWithToken(final User user, final Token token) {
        final String accessToken = token.getAccessToken();
        if (StringUtils.isNotBlank(accessToken)) {
            final String accessTokenLog = StringUtils.right(accessToken, 10);
            LOGGER.info("OAuth token is (last 10 digit): {}", accessTokenLog);
            user.setSid(accessToken);
            user.getAdditionalInfo().put(CommonConstants.SESSIONID, accessToken);
        }
    }

    private String getSignOutUrl(final HttpServletRequest request) {
        String signOutUri = null;
        OAuthAttributes oAuthAttributes =
                (OAuthAttributes) request.getSession().getAttribute(CommonConstants.OAUTH_ATTRIBUTES);
        if (Objects.nonNull(oAuthAttributes)) {
            OAuthCredentials oAuthCredentials = oAuthAttributes.getoAuthCredentials();
            if (Objects.nonNull(oAuthCredentials)) {
                final String endSessionUri = oAuthCredentials.getEndSession();
                Token token = oAuthAttributes.getToken();
                if (Objects.nonNull(token)) {
                    final URIBuilder uriBuilder = new URIBuilder();
                    uriBuilder.addParameter(CommonConstants.ID_TOKEN_HINT, token.getIdToken());
                    uriBuilder.addParameter(CommonConstants.POST_LOGOUT_REDIRECT_URI,applicationProperties.getProperty(CommonConstants.VIRGINAU_SIGNOUT_REDIRECT));
                    signOutUri = endSessionUri + uriBuilder.toString();
                }
            }
        }
        return signOutUri;
    }
    private void setOauthParam(Program program,HttpServletRequest request )
    {
        final String varId = request.getParameter(OAuthRequestParam.VARID.getValue());
        final String okta = request.getParameter(OAuthRequestParam.OKTA.getValue());
        final String code = request.getParameter(OAuthRequestParam.CODE.getValue());
        OAuthCredentials credentials = null;
        if (StringUtils.equalsIgnoreCase(okta, CommonConstants.YES_VALUE)) {
            credentials = oauthConfig.getOAuthCredentials(OAuthRequestParam.OKTA.getValue());
        } else {
            credentials = oauthConfig.getOAuthCredentials(varId);
        }
        final String redirectUrl = AppleUtil.getOAuthRedirectURI(request, credentials.getRedirectUri());
        Token token = oAuthTokenService.getTokenFomCode(code, credentials, redirectUrl);
        Map<String, Object> sessionConfigs = new HashMap<>();
        sessionConfigs.put(CommonConstants.OAUTH_CHECK_SESSION_IFRAME_URL,credentials.getCheckSessionIframe());
        sessionConfigs.put(CommonConstants.OAUTH_CLIENT_ID, credentials.getClientId());
        sessionConfigs.put(CommonConstants.OAUTH_TOKEN_SESSION_STATE, token.getSessionState());
        if(program.getSessionConfig()!=null)
            program.getSessionConfig().putAll(sessionConfigs);
        else
            program.setSessionConfig(sessionConfigs);
    }
}
