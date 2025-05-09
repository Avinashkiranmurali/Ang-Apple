package com.b2s.shop.common.order.var;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.integration.model.RedemptionResponse;
import com.b2s.rewards.apple.model.SessionUserInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.*;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component("varOrderManagerWF")
public class VAROrderManagerWF extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerWF.class);

    private static final String VAR_ID = "WF";
    private static final String LOCALE_STRING = "en_US";

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    protected String getLocale() {
        return LOCALE_STRING;
    }

    @Value("${disableMultipleAddressWellsFargoFeature:true}")
    private boolean disableMultiAddress;

    private enum SAMLAttributes {
        NAVBACKURL("navBackURL"),
        RAN("RAN"),
        ECN("ECN"),
        DEVICECHANNEL("DEVICECHANNEL"),
        VARID("varId"),
        PROGRAM_ID("programId"),
        POINTS_BALANCE("pointsBalance"),
        BROWSE_ONLY("browseOnly"),
        LOCALE("locale"),
        PROXY_USER_ID("proxyUserId"),
        SID("sid"),
        ANONYMOUS("anonymous"),
        CSID("csid"),
        KEYSTONE_BASE_URL("keystoneBaseUrl"),
        NAVFLAG("NAVFLAG"),
        KEEPALIVEURL("keepAliveUrl"),
        AUTHORIZED_FIRST_NAME("FIRSTNAME"),
        AUTHORIZED_LAST_NAME("LASTNAME"),
        DEVICEEXPERIENCE("DEVICEEXPERIENCE");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    @Override
    public boolean isSendOrderConfirmationEmailToUser() {
        return false;
    }

    @Override
    public User selectUser(HttpServletRequest request) throws B2RException {
        final User user ;
        Program program = null;
        boolean isUserFlagedBrowseOnly = false;
        CommonConstants.LoginType loginType = null;

        if (request.getParameter(CommonConstants.USER_ID) != null &&
            request.getParameter(CommonConstants.USER_ID).toLowerCase().contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())) {
            user = updateUser(request, new UserWF(), CommonConstants.COUNTRY_CODE_US, true);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        } else if (request.getParameter(CommonConstants.RELAY_STATE) == null) {
            user = selectLocalUser(request, new UserWF());
            //Getting browse only option from request
            isUserFlagedBrowseOnly = Boolean.parseBoolean(request.getParameter(CommonConstants.IS_BROWSE_ONLY));

            validateAnonymous(request, user);
            validateDeceased(request, user);

            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            // User creation for SAML login.
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes -> {
                LOGGER.info("WF SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value));
            });
            LOGGER.info("WF SAML UserPrincipal / RAN ----{}", request.getUserPrincipal().getName());
            loginType = CommonConstants.LoginType.SAML;

            final boolean anonymous;
            if (Objects.nonNull(request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE))) {
                anonymous = Boolean.valueOf((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE));
            } else {
                anonymous = false;
            }
            //Anonymous check
            if (anonymous) {
                user = setAnonymousUser(request, new UserWF());
            } else {
                user = getUser(request);
                samlAttributesValidation(user);
                //Getting browse only option from request
                isUserFlagedBrowseOnly = Boolean.parseBoolean(CommonConstants.getRequestAttribute(request, SAMLAttributes.BROWSE_ONLY.value));

                program = getProgram(user);
                setAdditionalUserInfo(request, user, program);
                setAuthorizedUserInfo(request, user, program);
                final String deviceExperience = user.getAdditionalInfo().get(CommonConstants.DEVICE_EXPERIENCE);
                if (org.apache.commons.lang3.StringUtils.isNotBlank(deviceExperience) && org.apache.commons.lang3.StringUtils.equalsIgnoreCase(deviceExperience, "N") && Objects.isNull(user.getFirstName())) {
                        user.setFirstName(CommonConstants.getRequestAttribute(request, SAMLAttributes.AUTHORIZED_FIRST_NAME.value));
                }
            }
        }
        user.setLoginType(loginType.getValue());
        final String deviceExperience = user.getAdditionalInfo().get(CommonConstants.DEVICE_EXPERIENCE);
        if ("N".equals(deviceExperience) && getSuppressTimeOut(user)) {
            user.setSuppressTimeoutAndKeepAliveEnabled(Boolean.TRUE);
        } else {
            user.setSuppressTimeoutAndKeepAliveEnabled(Boolean.FALSE);
        }
        initializeLocaleDependents(request, user);

        if(Objects.isNull(program)){
            program = getProgram(user);
        }
        updateAdditionalAttributesInSession(request, program, user, loginType);

        validateBrowseOnly(user, isUserFlagedBrowseOnly, program);

        if (loginType.equals(CommonConstants.LoginType.SAML)) {
            buildExternalUrls(request, program, (UserWF) user);
        }
        prepareUserAddress(user);

        return user;
    }

    private void setAuthorizedUserInfo(final HttpServletRequest request, final User user, final Program program) {
        final String programType = AppleUtil.getProgramConfigValueAsString(program, PROGRAM_TYPE);
        final String authorizedFirstName = CommonConstants.getRequestAttribute(request, SAMLAttributes.AUTHORIZED_FIRST_NAME.value);
        final String authorizedLastName = CommonConstants.getRequestAttribute(request, SAMLAttributes.AUTHORIZED_LAST_NAME.value);

        if (SMB_PROGRAM.equalsIgnoreCase(programType) &&
                (StringUtils.isNotBlank(authorizedFirstName) || StringUtils.isNotBlank(authorizedLastName))) {
            SessionUserInfo sessionUserInfo = new SessionUserInfo();
            sessionUserInfo.setFirstName(authorizedFirstName);
            sessionUserInfo.setLastName(authorizedLastName);
            Map<String, String> additionalInfo = user.getAdditionalInfo();
            LOGGER.info("WF OwnerType from Account Info: {}", additionalInfo.get(OWNER_TYPE));
            //Override ownerType as Authorized
            additionalInfo.put(OWNER_TYPE, AUTHORIZED);
            user.setSessionUserInfo(sessionUserInfo);
        }
    }

    private void buildExternalUrls(final HttpServletRequest request, final Program program, final UserWF user) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);

        setSessionExternalParams(request);
        setKeystoneUrls(externalUrls, CommonConstants.getRequestAttribute(request, SAMLAttributes.KEYSTONE_BASE_URL.value));

        final boolean isTwoWayNavEnabled =
                AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.IS_TWO_WAY_NAV_ENABLED);
        if (isTwoWayNavEnabled) {
            processTwoWayNavigation(request, user, program, externalUrls);
        } else {
            LOGGER.info("WF Two way Navigation is disabled. Loading keep alive URLs from VPC ....");
            buildKeepAliveRequestUrlsFromVpc(program, externalUrls);
        }

        //Set NavBackUrl to Merchandise Home page
        if (Objects.isNull(externalUrls.get(ExternalUrlConstants.NAVIGATE_BACK_URL))) {
            externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL,
                    CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value));
        }

        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_JSONP,
            AppleUtil.getValueFromProgramConfig(program, ExternalUrlConstants.KEEP_ALIVE_JSONP, "false"));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    /**
     * Update 10/21/2021
     *  R-04665 - Wells Fargo - 2 Way Navigation Request
     *          Keep live URL : Keystone will be sending the Epsilon and WF keepalive URLs in the SAML payload to Apple, reference “keepAliveUrl”.
     *              - NAVFLAG=1, keystone will send keepAliveUrl with the Epsilon keepalive URL only.
     *              - NAVFLAG=2, keystone will send keepAliveUrl with the Epsilon keepalive URL and the WF keepalive URL separated by a comma.
     *
     *          SIGN OUT Url : based on environment properties
     *                          cp.1.signOutUrl
     *                          cp.2.signOutUrl
     *                          smb.1.signOutUrl
     *                          smb.2.signOutUrl
     *
     */
    private void processTwoWayNavigation(final HttpServletRequest request, final UserWF user, final Program program, final HashMap<String, Serializable> externalUrls) {
        final String navFlag = CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVFLAG.value);
        if (StringUtils.isNotBlank(navFlag)) {
            LOGGER.info("NAVFLAG SAML received : {}", navFlag);
            user.setNavflag(navFlag);
            final String programType = AppleUtil.getProgramConfigValueAsString(program, PROGRAM_TYPE);
            buildSignOutUrl(externalUrls, programType, navFlag);
            buildTimeOutUrls(externalUrls, programType, navFlag);
            buildKeepAliveRequestUrlsFromSaml(CommonConstants.getRequestAttribute(request, SAMLAttributes.KEEPALIVEURL.value), externalUrls);
        } else {
            LOGGER.error("NAVFLAG Saml attribute not received or is blank, while 2 Way navigation is enabled");
        }
    }

    private void buildKeepAliveRequestUrlsFromVpc(final Program program, final HashMap<String, Serializable> externalUrls) {
        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL,
                AppleUtil.getProgramConfigValueAsString(program, ExternalUrlConstants.KEEP_ALIVE_URL));
    }

    /**
     * S-21557
     * For NAVFLAG=1, keystone will send keepAliveUrl with the Epsilon keepalive URL only.
     * For NAVFLAG=2, keystone will send keepAliveUrl with the Epsilon keepalive URL & WF keepalive URL separated by comma.
     *
     * @param keepAliveUrlFromSaml
     * @param externalUrls
     */
    private void buildKeepAliveRequestUrlsFromSaml(final String keepAliveUrlFromSaml, final HashMap<String, Serializable> externalUrls) {
        if (StringUtils.isNotBlank(keepAliveUrlFromSaml)) {
            LOGGER.info("2 Way Navigation - Keep alive URL received : {}", keepAliveUrlFromSaml);

            final String[] keepAliveUrlSamlArray = keepAliveUrlFromSaml.split(COMMA);

            externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL_POINTS_BANK, keepAliveUrlSamlArray[0]);
            if (keepAliveUrlSamlArray.length > 1) {
                externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL, keepAliveUrlSamlArray[1]);
            } else {
                // The following URL is already set from incoming SAML attributes. it needs to be removed if it exists in SAML (keepAliveUrl)
                externalUrls.remove(ExternalUrlConstants.KEEP_ALIVE_URL);
            }
        }
    }


    /**
     * Set timeOutUrls only if Nav flag 2
     * Nav flag 1 : Bakkt will not call the WF session kill url or the epsilon one way downgrade notifier. 
     *
     * @param externalUrls
     * @param programType
     * @param navFlag
     */
    private void buildTimeOutUrls(HashMap<String, Serializable> externalUrls, final String programType, final String navFlag) {
        //Set timeOutUrls only if Nav flag 2
        if (CommonConstants.WF_TWO_WAY_NAV_VALUE.equalsIgnoreCase(navFlag)) {

            final List<String> partnerTimeOutUrls = List.of(
                    applicationProperties.getProperty(CommonConstants.VAR_WELLSFARGO + DOT + navFlag + DOT + CommonConstants.PARTNER_TIME_OUT_URL, StringUtils.EMPTY),
                    applicationProperties.getProperty(programType + DOT + navFlag + DOT + CommonConstants.PARTNER_TIME_OUT_URL, StringUtils.EMPTY)
            );

            LOGGER.debug("WF Partner TimeOutUrls ---- {}", partnerTimeOutUrls);
            externalUrls.put(ExternalUrlConstants.PARTNER_TIME_OUT_URLS, new ArrayList<>(partnerTimeOutUrls));
        }

        externalUrls.put(ExternalUrlConstants.TIME_OUT_URL, applicationProperties
            .getProperty(CommonConstants.VAR_WELLSFARGO + DOT + ExternalUrlConstants.TIME_OUT_URL, StringUtils.EMPTY));
    }

    private void buildSignOutUrl(HashMap<String, Serializable> externalUrls, final String programType, final String navFlag) {
        final String signOutUrl =
            applicationProperties.getProperty(programType + DOT + navFlag + DOT + ExternalUrlConstants.SIGN_OUT_URL);
        //Set partnerSignOutUrls only if Nav flag 2
        if (CommonConstants.WF_TWO_WAY_NAV_VALUE.equalsIgnoreCase(navFlag)) {
            final List<String> partnerSignOutUrls = List.of(
                applicationProperties.getProperty(programType + DOT + navFlag + DOT + CommonConstants.PARTNER_SIGN_OUT_URL, StringUtils.EMPTY)
            );
            LOGGER.debug("WF Partner SignOutUrls ---- {}", partnerSignOutUrls);
            externalUrls.put(ExternalUrlConstants.PARTNER_SIGN_OUT_URLS, new ArrayList<>(partnerSignOutUrls));
        }
        externalUrls.put(ExternalUrlConstants.SIGN_OUT_URL, signOutUrl);
    }

    private void validateAnonymous(HttpServletRequest request, User user) {
        //Enable the checkbox at 5-box login for ANONYMOUS user to be added to User Object
        if (Objects.nonNull(request.getParameter(CommonConstants.IS_ANONYMOUS))) {
            user.setAnonymous(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ANONYMOUS)));
        }
    }

    private void validateDeceased(HttpServletRequest request, User user) {
        String deceased = request.getParameter(CommonConstants.DECEASED);
        if (Objects.nonNull(deceased) && Boolean.parseBoolean(deceased)) {
            user.setDeceased(true);
            user.setFirstName(null);
            user.setLastName(null);
        }
    }

    private void validateBrowseOnly(User user, boolean isUserFlagedBrowseOnly, Program program) {
        //Setting browse only option for user
        if (MapUtils.isNotEmpty(program.getConfig())) {
            final Object browseOnlyAttributeFromConfig = program.getConfig().get(SAMLAttributes.BROWSE_ONLY.value);
            final boolean isProgramEnableForBrowseOnly = Boolean.parseBoolean(browseOnlyAttributeFromConfig != null ? browseOnlyAttributeFromConfig.toString() : null);
            user.setBrowseOnly(isProgramEnableForBrowseOnly && isUserFlagedBrowseOnly);
        }
    }

    private void setAdditionalUserInfo(final HttpServletRequest request, final User user, final Program program) throws B2RException {
        // Get the User Information
        final AccountInfo accountInfo = getVISImplementation(user, program.getIsLocal()).getUserProfile(user.getVarId(),
            user.getUserId(), user.getAdditionalInfo());
        if (accountInfo != null && accountInfo.getUserInformation() != null) {
            LOGGER.debug("disableMultiAddress is {}, when true additional addresses are omitted", disableMultiAddress);
            accountInfo.getUserInformation().copyDataToUser(user, disableMultiAddress);

            if (accountInfo.getUserInformation().isDeceased()) {
                user.setFirstName(null);
                user.setLastName(null);
            }
        }
        if (accountInfo != null && accountInfo.getAccountBalance() != null) {
            user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
            if (Objects.isNull(user.getAdditionalInfo())) {
                user.setAdditionalInfo(new HashMap<>());
            }
            user.getAdditionalInfo().put(CommonConstants.VIS_CURRENCY, accountInfo.getAccountBalance().getCurrency());
        }
        user.getAdditionalInfo().put(CommonConstants.SESSIONID, user.getSid());
        user.getAdditionalInfo().put(CommonConstants.VIS_ADD_INFO_ECN,
                CommonConstants.getRequestAttribute(request, SAMLAttributes.ECN.value));
        user.getAdditionalInfo().put(CommonConstants.VIS_BOOKING_CHANNEL,
                CommonConstants.getRequestAttribute(request, SAMLAttributes.DEVICECHANNEL.value));
        user.getAdditionalInfo().put(CommonConstants.PROGRAM_ID,
                CommonConstants.getRequestAttribute(request, SAMLAttributes.PROGRAM_ID.value));
        user.getAdditionalInfo().put(CommonConstants.DEVICE_EXPERIENCE,
                CommonConstants.getRequestAttribute(request, SAMLAttributes.DEVICEEXPERIENCE.value));
    }

    @Override
    protected User getUser(HttpServletRequest request) {
        User user = new UserWF();
        // User Id will be part of subject
        user.setUserId(request.getUserPrincipal().getName());
        user.setVarId(CommonConstants.getRequestAttribute(request, SAMLAttributes.VARID.value));
        user.setProgramId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROGRAM_ID.value));
        if (StringUtils.isNumeric(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value))){
            user.setPoints(valueOf(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value)));
        }
        final String locale = CommonConstants.getRequestAttribute(request, SAMLAttributes.LOCALE.value);
        if (isNotBlank(locale)) {
            user.setLocale(LocaleUtils.toLocale(locale));
        }
        user.setSid(CommonConstants.getRequestAttribute(request, SAMLAttributes.SID.value));
        user.setProxyUserId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROXY_USER_ID.value));
        user.setCsid(CommonConstants.getRequestAttribute(request, SAMLAttributes.CSID.value));

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.SESSIONID, user.getSid());
        additionalInfo.put(CommonConstants.VIS_ADD_INFO_ECN, CommonConstants.getRequestAttribute(request, SAMLAttributes.ECN.value));
        additionalInfo.put(CommonConstants.VIS_BOOKING_CHANNEL, CommonConstants.getRequestAttribute(request, SAMLAttributes.DEVICECHANNEL.value));

        return user;
    }

    private void setSessionExternalParams(final HttpServletRequest request) {
        request.getSession().setAttribute(ExternalUrlConstants.NAVIGATE_BACK_URL, CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value));
    }

    private void samlAttributesValidation(User user) {
        if(StringUtils.isEmpty(user.getVarId()) || StringUtils.isEmpty(user.getProgramId())) {
            LOGGER.error("VAR ID and PROGRAM ID cannot be empty");
            throw new IllegalArgumentException("VAR ID and PROGRAM ID cannot be empty");
        }
    }

    @Override
    public boolean placeOrder(final Order order, final User user, final Program program) {
        try {
            //used by demo, wf vars
            final RedemptionResponse redemptionResponse =
                getVISImplementation(user, program.getIsLocal()).performRedemption(order, user, program);
            //assign/update order with the redemption response
            if (Objects.isNull(redemptionResponse)) {
                return false;
            }
            order.setVarOrderId(redemptionResponse.getVarOrderId());
            //adjust user points
            getBalanceUserPoints(order, user);
            message = getMessage(redemptionResponse);

        } catch (final Exception ex) {
            LOGGER
                .error("Error updating user Var details in VarIntegrationService...Exception trace:  ", ex.getMessage(),
                    ex);
            insertMessageVISException(order, user);
        }

        return message.isSuccess();
    }

    @Override
    public boolean cancelOrder(final Order order, final User user, Program program) {
        boolean isSuccess = true;
        try {
            // VIS call
            getVISImplementation(user, CommonConstants.IS_LOCAL_FALSE).performPartialCancelRedemption(order, user, program);
        } catch (final Exception ex) {
            LOGGER.error("Error updating user Var details in VAROrderManagerWF...Exception trace:  ", ex.getMessage(),
                ex);
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * This method simply serves to extract some locale setting logic that get pushed into various objects.
     * @param request
     * @param user
     */
    private void initializeLocaleDependents(HttpServletRequest request, User user) {
        final String country = CommonConstants.COUNTRY_CODE_US;
        //SPECIAL HANDLING TOCOMES IN THE  FORCE THE USER COUNTRY TO THE CATCOUNTRY THAT SAML URL PARAMETER
        if(!country.equalsIgnoreCase(user.getCountry())){
            user.setAddr1(null);
            user.setAddr2(null);
            user.setCity(null);
            user.setZip(null);
            user.setState(null);
            user.setCountry(country);
        }

        user.setLocale(getLocale(request, CommonConstants.LOCALE_EN_US));

    }

    public boolean getSuppressTimeOut(final User user) throws B2RException {

        final String suppressTimeoutAndKeepAliveEnabled =
            varProgramConfigDao.getVarProgramConfigValue(user.getVarId(),
                user.getProgramId(),
                CommonConstants.SUPPRESS_TIMEOUT_WARNING_AND_KEEPALIVE_FOR_NATIVEAPP,
                Boolean.TRUE.toString());
        return Boolean.parseBoolean(suppressTimeoutAndKeepAliveEnabled);
    }
}
