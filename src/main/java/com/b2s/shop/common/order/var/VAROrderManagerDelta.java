package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.model.Program;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * @author marumugam 2018-05-08
 */
@Component("varOrderManagerDelta")
public class VAROrderManagerDelta extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerDelta.class);

    private static final String VAR_ID = "Delta";

    private enum SAMLAttributes {
        USERID("skymiles_number"),
        POINTSBALANCE("pointsBalance"),
        VARID("varId"),
        PROGRAMID("programId"),
        LOCALE("locale"),
        NAVBACKURL("navBackURL"),
        KEEPALIVEURL("keepAliveUrl"),
        BROWSEONLY("browseOnly"),
        ANONYMOUS("anonymous"),
        PROXY_USER_ID("proxyUserId"),
        CSID("csid"),
        KEYSTONE_BASE_URL("keystoneBaseUrl");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final User user ;
        Program program = null;
        CommonConstants.LoginType loginType = null;
        if (request.getParameter(CommonConstants.USER_ID) != null &&
            request.getParameter(CommonConstants.USER_ID).toLowerCase().contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())) {
            user = updateUser(request, new User(), null, true);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        } else if (request.getParameter(CommonConstants.RELAY_STATE) == null) {
            user = selectLocalUser(request, new User());
            //Enable the checkbox at 5-box login for ANONYMOUS user to be added to User Object
            if (Objects.nonNull(request.getParameter(CommonConstants.IS_ANONYMOUS))) {
                user.setAnonymous(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ANONYMOUS)));
            }
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes ->
                LOGGER.info("Delta SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value)));
            LOGGER.info("Delta SAML UserPrincipal ----{}", request.getUserPrincipal().getName());
            loginType = CommonConstants.LoginType.SAML;

            final boolean anonymous = Objects.nonNull(request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE))?
                Boolean.valueOf((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE)): false;

            //Anonymous check
            if (anonymous) {
                user = setAnonymousUser(request, new User());
            } else {
                user = getUserFromSAMLAttributes(request);
                initializeLocaleDependents(request, user, CommonConstants.LOCALE_EN_US,
                    CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);

                retrieveUserAdditionalInfoWithProgramId(user);
                final AccountInfo accountInfo = getAccountInfo(user, program.getIsLocal(),
                    varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl);

                setUserInfo(user, accountInfo);
            }
        }
        user.setLoginType(loginType.getValue());
        if(Objects.isNull(program)){
            initializeLocaleDependents(request, user, CommonConstants.LOCALE_EN_US, CommonConstants.COUNTRY_CODE_US);
            program = getProgram(user);
        }
        updateAdditionalAttributesInSession(request, program, user, loginType);
        if(CommonConstants.LoginType.SAML.equals(loginType)){
            //Set the URLs from SAML response
            setSessionUrls(request);
            setKeepAliveCOREUrl(request, program);
        }
        prepareUserAddress(user);
        final boolean verifyLocaleFlag = (Boolean) program.getConfig().getOrDefault(CommonConstants.VERIFY_LOCALE_WITH_COUNTRY,Boolean.FALSE);
        if(verifyLocaleFlag && !verifyLocaleWithCountry(user)){
                LOGGER.error(
                    "The Selected User does not match locale and country. User Id: " + user.getUserId() +
                        "locale: "+user.getLocale() +" country: "+user.getCountry());
                throw new B2RException("The Selected User does not match locale and country. User Id: " + user.getUserId() +
                    "locale: "+user.getLocale() +" country: "+user.getCountry());
        }
        return user;
    }

    /**
     * Set User Additional info based on Account Info
     *
     * @param user
     * @param accountInfo
     */
    private void setUserInfo(final User user, final AccountInfo accountInfo){
        if (Objects.nonNull(accountInfo)) {
            if (Objects.nonNull(accountInfo.getUserInformation())) {
                accountInfo.getUserInformation().copyDataToUser(user);
            }
            if (Objects.nonNull(accountInfo.getAccountBalance())) {
                user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
                if (user.getAdditionalInfo() == null) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                user.getAdditionalInfo()
                    .put(CommonConstants.VIS_CURRENCY, accountInfo.getAccountBalance().getCurrency());
            }
        }
    }

    private void setSessionUrls(final HttpServletRequest request) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL, CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value));
        externalUrls.put(ExternalUrlConstants.KEEP_ALIVE_URL, CommonConstants.getRequestAttribute(request, SAMLAttributes.KEEPALIVEURL.value));

        setKeystoneUrls(externalUrls, CommonConstants.getRequestAttribute(request, SAMLAttributes.KEYSTONE_BASE_URL.value));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private void setKeepAliveCOREUrl(final HttpServletRequest request, final Program program) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    private User getUserFromSAMLAttributes(final HttpServletRequest request) {
        User user = new User();
        user.setUserId((String)request.getAttribute(SAMLAttributes.USERID.value));
        user.setLocale(LocaleUtils.toLocale((String) request.getAttribute(SAMLAttributes.LOCALE.value)));
        user.setVarId(VAR_ID);
        user.setProgramId((String) request.getAttribute(SAMLAttributes.PROGRAMID.value));
        user.setPoints(Integer.parseInt((String) request.getAttribute(SAMLAttributes.POINTSBALANCE.value)));
        user.setBrowseOnly((Boolean.valueOf((String)request.getAttribute(SAMLAttributes.BROWSEONLY.value))));
        if (request.getUserPrincipal() == null || StringUtils.isBlank(request.getUserPrincipal().getName())) {
            LOGGER.error("User id(request.getUserPrincipal().getName()) is blank.");
            throw new IllegalArgumentException("User id(request.getUserPrincipal().getName()) is blank.");
        }
        user.setProxyUserId(CommonConstants.getRequestAttribute(request, SAMLAttributes.PROXY_USER_ID.value));
        user.setCsid(CommonConstants.getRequestAttribute(request, SAMLAttributes.CSID.value));
        return user;
    }

    /**
     * This method is used to get User points. It has logic to avoid user balance is low issue by returning payment max
     * limit of var program payment option. Do not use this method to populate user points in User object.
     *
     * @param user    -current user
     * @param program
     * @return int
     * @throws B2RException
     */
    @Override
    public int getUserPoints(final User user, final Program program)
        throws B2RException {
        if (Objects.nonNull(program) && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            final Optional<String> sid=Optional.ofNullable(user.getSid());
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
        }

    }

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

}