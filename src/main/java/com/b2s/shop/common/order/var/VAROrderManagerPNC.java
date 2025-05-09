package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

import static java.lang.Integer.valueOf;

@Component("varOrderManagerPNC")
public class VAROrderManagerPNC extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerPNC.class);

    private static final String VAR_ID = "PNC";
    private static final String LOCALE_STRING = "en_US";

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    protected String getLocale() {
        return LOCALE_STRING;
    }

    private enum SAMLAttributes {
        NAVBACKURL("navBackURL"),
        VARID("varId"),
        PROGRAM_ID("programId"),
        POINTS_BALANCE("pointsBalance"),
        BROWSE_ONLY("browseOnly"),
        CSID("csid"),
        LOCALE("locale"),
        KEEPALIVEURL("keepAliveUrl"),
        COUNTRY("country"),
        ANONYMOUS("anonymous"),
        SID("sid"),
        PROXY_USER_ID("proxyUserId"),
        PROFILEID("ProfileId"),
        PROGRAMCODE("ProgramCode"),
        KEYSTONE_BASE_URL("keystoneBaseUrl");

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
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final User user;
        CommonConstants.LoginType loginType = null;
        Program program = null;
        // 5 Box Login
        if (Objects.isNull(request.getParameter(CommonConstants.RELAY_STATE))) {
            user = selectLocalUser(request, new User());
            //Enable the checkbox at 5-box login for ANONYMOUS user to be added to User Object
            if(Objects.nonNull(request.getParameter(CommonConstants.IS_ANONYMOUS))){
                user.setAnonymous(Boolean.parseBoolean(request.getParameter(CommonConstants.IS_ANONYMOUS)));
            }
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            // SAML Flow
            Arrays.stream(SAMLAttributes.values()).forEach(samlAttributes ->
                LOGGER.info("PNC SAML attributes ------{} ### {}", samlAttributes.value,
                    request.getAttribute(samlAttributes.value)));
            LOGGER.info("PNC SAML UserPrincipal ----{}", request.getUserPrincipal().getName());

            //Anonymous check
            if (isAnonymous(request)) {
                user = setAnonymousUser(request, new User());
                loginType = CommonConstants.LoginType.ANONYMOUS;
            } else {
                user = getUser(request);
                initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
                program = getProgram(user);
                setUserVimsAdditionalInfo(request, user, program);

                final AccountInfo accountInfo = getAccountInfo(user, program.getIsLocal(),
                    varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl);

                setUserAdditionalInfo(request, user, program, accountInfo);

                loginType = CommonConstants.LoginType.SAML;
            }
        }
        user.setLoginType(loginType.getValue());
        if (Objects.isNull(program)) {
            initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,CommonConstants.COUNTRY_CODE_US);
            program = getProgram(user);
        }
        request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
        setSessionTimeOut(request, user);

        //Bag Menu URLs from DB is having less precedence than SAML response
        addOrUpdateExternalUrls(request, program, user.getLocale().toString(), loginType);

        //Set the URLs from SAML response
        setExternalUrlsFromSaml(request);
        prepareUserAddress(user);
        return user;
    }

    private boolean isAnonymous(HttpServletRequest request) {
        if (Objects.nonNull(request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE))) {
            return Boolean.valueOf((String) request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE));
        }
        return false;
    }

    private void setUserAdditionalInfo(HttpServletRequest request, User user, Program program, AccountInfo accountInfo) {
        if(Objects.nonNull(accountInfo)){
            if(Objects.nonNull(accountInfo.getUserInformation())){
                accountInfo.getUserInformation().copyDataToUser(user);
            }
            if(Objects.nonNull(accountInfo.getAccountBalance())){
                user.setPoints(accountInfo.getAccountBalance().getPointsBalance());
                if (Objects.isNull(user.getAdditionalInfo())) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                user.getAdditionalInfo().put(CommonConstants.VIS_CURRENCY, accountInfo.getAccountBalance().getCurrency());
            }
        }else{
            if(program.getIsLocal() && Objects.nonNull(request.getAttribute(CommonConstants.POINTS_BALANCE))){
                user.setPoints(valueOf(CommonConstants.getRequestAttribute(request, SAMLAttributes.POINTS_BALANCE.value)));
            }
        }
    }

    private void setUserVimsAdditionalInfo(final HttpServletRequest request, final User user, final Program program) {
        //S-17565 -Setting AdditionalInfos - ProfileId & ProgramCode - For PNC
        if (Objects.nonNull(program.getConfig().get(CommonConstants.VIMS_ADDITIONAL_INFO))) {
            final String vimsAdditionalInfo = program.getConfig().get(CommonConstants.VIMS_ADDITIONAL_INFO).toString();
            if (StringUtils.isNotBlank(vimsAdditionalInfo)) {
                setAdditionalInfo(request, user, vimsAdditionalInfo);
            }
        }
    }

    private void setAdditionalInfo(HttpServletRequest request, User user, String vimsAdditionalInfo) {
        for (String additionalInfo : vimsAdditionalInfo.split(",")) {
            if (StringUtils.isNotBlank(additionalInfo)) {
                if (Objects.isNull(user.getAdditionalInfo())) {
                    user.setAdditionalInfo(new HashMap<>());
                }
                final String additionalInfoAttribute = (String) request.getAttribute(additionalInfo);
                if(StringUtils.isNotBlank(additionalInfoAttribute)){
                    user.getAdditionalInfo().put(additionalInfo, additionalInfoAttribute);
                }
            }
        }
    }

    @Override
    protected User getUser(HttpServletRequest request) {
        User user = new User();
        final String programId = (String)request.getAttribute(CommonConstants.PROGRAM_ID);
        final String userId = request.getUserPrincipal().getName();
        final String sId =  (String)request.getAttribute(CommonConstants.SID);
        final String csId =  (String)request.getAttribute(CommonConstants.CSID);
        user.setProgramId(programId);
        user.setVarId(getVARId());
        user.setUserId(userId);
        user.setSid(sId);
        user.setCsid(csId);
        user.setProxyUserId(CommonConstants.getRequestAttribute(request,
                VAROrderManagerPNC.SAMLAttributes.PROXY_USER_ID.value));

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.SESSIONID, user.getSid());

        return user;
    }

    private void setExternalUrlsFromSaml(final HttpServletRequest request) {
        final HashMap<String, Serializable> externalUrls = getExternalUrlsFromRequest(request);
        final String navBackUrl = CommonConstants.getRequestAttribute(request, SAMLAttributes.NAVBACKURL.value);
        if(StringUtils.isNotBlank(navBackUrl)){
            externalUrls.put(ExternalUrlConstants.NAVIGATE_BACK_URL, navBackUrl);
        }

        setKeystoneUrls(externalUrls, CommonConstants.getRequestAttribute(request, SAMLAttributes.KEYSTONE_BASE_URL.value));

        request.getSession().setAttribute(ExternalUrlConstants.EXTERNAL_URLS, externalUrls);
    }

    @Override
    public int getUserPoints(User user, Program program) throws B2RException {
        if (Objects.nonNull(program) && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), user.getUserId(), user.getAdditionalInfo());
        }
    }

}