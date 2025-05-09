package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.common.util.CommonConstants.getRequestAttribute;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component("varOrderManagerSCOTIA")
public class VAROrderManagerSCOTIA extends GenericVAROrderManager  {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerSCOTIA.class);

    protected String getSignOutUrl(final Locale locale) {
        final StringBuffer signOutUrlName = new StringBuffer("scotia.signOutUrl");
        if( locale != null ) {
            signOutUrlName.append('.' +locale.toString());
        }
        final String signOutUrl = applicationProperties.getProperty(signOutUrlName.toString());
        if (StringUtils.isBlank(signOutUrl)) {
            return "https://www.scotiarewards.com/LoggedOut.aspx";
        } else {
            return signOutUrl;
        }
    }

    protected String getTimeOutUrl(final Locale locale, final Program program) {
        String sessionTimeoutUrlKey = program.getVarId().toLowerCase() + "." + program.getProgramId().toLowerCase() + "." + ExternalUrlConstants.SIGN_OUT_URL;
        if (Objects.nonNull(locale)) {
            sessionTimeoutUrlKey += "." + locale.toString();
        }
        String sessionTimeOutUrlValue = applicationProperties.getProperty(sessionTimeoutUrlKey);

        // if the varId.programId.signOutUrl.locale does not exist,
        // then check for varId.signOutUrl.locale
        if (StringUtils.isEmpty(sessionTimeOutUrlValue)) {
            sessionTimeoutUrlKey = program.getVarId().toLowerCase() + "." + ExternalUrlConstants.SIGN_OUT_URL;
            if (Objects.nonNull(locale)) {
                sessionTimeoutUrlKey += "." + locale.toString();
            }

            sessionTimeOutUrlValue = applicationProperties.getProperty(sessionTimeoutUrlKey);
        }

        return sessionTimeOutUrlValue;
    }

    private String getKeepAliveUrl() {
        return applicationProperties.getProperty(CommonConstants.SCOTIA_KEEP_ALIVE_URL);
    }

    private enum SAMLAttributes {
        NAVBACKURL("navBackURL"),
        USERID("participantId"),
        AGENT_ID("proxyUserId"),
        VARID("varId"),
        PROGRAM_ID("programId"),
        POINTS_BALANCE("pointsBalance"),
        BROWSE_ONLY("browseOnly"),
        LOCALE("locale"),
        SID("sid"),
        CSID("csid"),
        KEYSTONE_BASE_URL("keystoneBaseUrl"),
        PROXY_USER_ID("proxyUserId"),
        SOURCE("source");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    // TODO Need to verify the below attributes and assertion fields with Josh.
    // var id for scotia
    private static final String VAR_ID = "SCOTIA";
    private static final String WEBSITE = "website";
    private static final String OVERRIDE_CLIENT_HEADER = "overrideClientHeader";
    private static final String DISPLAY_CLIENT_HEADER = "displayClientHeader";

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException{
        final User user;
        Program program = null;
        CommonConstants.LoginType loginType = null;
        if (request.getParameter(CommonConstants.RELAY_STATE) == null) {
            user = selectLocalUser(request, new UserScotia());
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            loginType = CommonConstants.LoginType.SAML;
            // log the saml request attributes
            LOGGER.debug("Scotia Login Request ---- {}", CommonConstants.getRequestAttributes(request));
            user = getUser(request);

            initializeLocaleDependents(request, user);
            program = getProgram(user);

            //S-22087: Only for SCOTIA/SCENE, If source = website, show Header, else don't display Header.
            setDisplayClientHeader(request, program);

            // Get the User Information
            final AccountInfo accountInfo = getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl);

            if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.POPULATE_USER_INFO)) {
                populateUserInfo(user, accountInfo);
            }

            if (Objects.nonNull(accountInfo) && Objects.nonNull(accountInfo.getAccountBalance())) {
                user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
            }

            samlAttributesValidation(user);
        }
        user.setLoginType(loginType.getValue());

        if(Objects.isNull(program)){
            initializeLocaleDependents(request, user);
            program = getProgram(user);
        }

        // Set the Bag Menu URLs from DB
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);
        // Override Bag Menu URLs from SAML response
        if(CommonConstants.LoginType.SAML.equals(loginType)){
            setKeepAliveRequestUrl(request, user.getLocale(),program);
        }

        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);
        prepareUserAddress(user);
        return user;
    }

    /**
     * Set Program Config "displayClientHeader" based on VPC and SAML response
     * S-22087: Only for SCOTIA/SCENE, If source = website, show Header, else don't display Header.
     *
     * @param request
     * @param program
     */
    private void setDisplayClientHeader(final HttpServletRequest request, final Program program) {
        if (getProgramConfigValueAsBoolean(program, OVERRIDE_CLIENT_HEADER)) {
            if (WEBSITE.equalsIgnoreCase(getRequestAttribute(request, SAMLAttributes.SOURCE.value))) {
                program.getConfig().put(DISPLAY_CLIENT_HEADER, Boolean.TRUE);
            } else {
                program.getConfig().put(DISPLAY_CLIENT_HEADER, Boolean.FALSE);
            }
        }
    }

    private void populateUserInfo(final User user, final AccountInfo accountInfo){
        if (Objects.nonNull(accountInfo) && Objects.nonNull(accountInfo.getUserInformation())) {
            accountInfo.getUserInformation().copyDataToUser(user);
        }
    }

    private void samlAttributesValidation(User user) {
        if(StringUtils.isEmpty(user.getVarId()) || StringUtils.isEmpty(user.getProgramId())) {
            LOGGER.error("VAR ID and PROGRAM ID cannot be empty");
            throw new IllegalArgumentException("VAR ID and PROGRAM ID cannot be empty");
        }
    }

    @Override
    protected User getUser(HttpServletRequest request) {
        UserScotia user = new UserScotia();
        user.setUserId(request.getUserPrincipal().getName());
        user.setAgentId(CommonConstants.getRequestAttribute(request, SAMLAttributes.AGENT_ID.value));
        user.setVarId(CommonConstants.getRequestAttribute(request, SAMLAttributes.VARID.value));
        user.setProgramId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROGRAM_ID.value));
        if (StringUtils.isNumeric(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value))){
            user.setPoints(valueOf(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value)));
        }
        final String locale = CommonConstants.getRequestAttribute(request, SAMLAttributes.LOCALE.value);
        if (isNotBlank(locale)) {
            user.setLocale(LocaleUtils.toLocale(locale));
        }
        user.setCsid(CommonConstants.getRequestAttribute(request, SAMLAttributes.CSID.value));
        user.setSid(CommonConstants.getRequestAttribute(request, SAMLAttributes.SID.value));
        final String browseOnly = CommonConstants.getRequestAttribute(request, SAMLAttributes.BROWSE_ONLY.value);
        if(isNotBlank(browseOnly)) {
            user.setBrowseOnly(Boolean.valueOf(browseOnly));
        }
        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put("sessionId", user.getSid());
        user.setProxyUserId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROXY_USER_ID.value));

        return user;
    }

    private void setKeepAliveRequestUrl(HttpServletRequest request, final Locale userLocale,final Program program) {
        setSessionExternalParams(request);
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        final String navBackUrl = (String) externalUrls.get(ExternalUrlConstants.NAVIGATE_BACK_URL);
        // if navigateBackUrl does not exists in VPC, then configure based on SAML Attribute value
        if (StringUtils.isEmpty(navBackUrl)) {
            externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL, CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value));
        }

        externalUrls.put(ExternalUrlConstants.TIME_OUT_URL, getTimeOutUrl(userLocale, program));
        externalUrls.put(ExternalUrlConstants.SIGN_OUT_URL, getSignOutUrl(userLocale));
        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL, getKeepAliveUrl());
        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_JSONP, AppleUtil.getValueFromProgramConfig(program,ExternalUrlConstants.KEEP_ALIVE_JSONP,"true"));

        setKeystoneUrls(externalUrls, CommonConstants.getRequestAttribute(request, SAMLAttributes.KEYSTONE_BASE_URL.value));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private void setSessionExternalParams(final HttpServletRequest request) {
        request.getSession().setAttribute(ExternalUrlConstants.NAVIGATE_BACK_URL, CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value));
    }

    @Override
    public int getUserPoints(final User user, final Program program) throws B2RException {
        if (program != null && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            // Some how additionalInfo is empty that is why adding this again.
            // TODO : remove later - investigate where the additonInfo is getting empty
            final Map<String, String> additonalInfo =  new HashMap();
            additonalInfo.put( "sessionId", user.getSid() );
            user.setAdditionalInfo(additonalInfo);
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), user.getUserId(), user.getAdditionalInfo());
        }
    }

    /**
     * This method simply serves to extract some locale setting logic that get pushed into various objects.
     * @param request
     * @param user
     */
    private static void initializeLocaleDependents(HttpServletRequest request, User user) {
        final String country = CommonConstants.COUNTRY_CODE_CA;
        //SPECIAL HANDLING TO FORCE THE USER COUNTRY TO THE CATCOUNTRY THAT COMES IN THE SAML URL PARAMETER
        if(!country.equalsIgnoreCase(user.getCountry())){
            user.setAddr1(null);
            user.setAddr2(null);
            user.setCity(null);
            user.setZip(null);
            user.setState(null);
            user.setCountry(country);
        }

        user.setLocale(getLocale(request, request.getParameter("locale")));

    }

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

}

